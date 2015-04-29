package it.flaten.irc;

import it.flaten.chat.Chat;
import it.flaten.irc.client.Client;
import it.flaten.irc.client.ProtocolMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Irc extends Plugin {
    private File configFile;
    private Configuration config;

    private Chat chat;
    private final HashMap<UUID, Client> clients = new HashMap<>();

    @Override
    public void onEnable() {
        this.getLogger().info("Loading configuration...");

        this.configFile = new File(this.getDataFolder(), "config.yml");

        if (!this.configFile.exists()) {
            this.getDataFolder().mkdir();

            try {
                Files.copy(this.getResourceAsStream("config.yml"), this.configFile.toPath());
            } catch (IOException exception) {
                this.getLogger().severe("Failed to copy default configuration.");
                exception.printStackTrace();
            }
        }

        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
        } catch (IOException exception) {
            this.getLogger().severe("Failed to load config.yml. This plugin will not do anything.");
            return;
        }

        this.getLogger().info("Fetching Chat plugin instance...");

        this.chat = (Chat) this.getProxy().getPluginManager().getPlugin("Chat");

        if (this.chat == null) {
            this.getLogger().warning("Unable to find Chat plugin. This plugin will not do anything.");
            return;
        }

        this.getLogger().info("Registering event handlers...");

        this.getProxy().getPluginManager().registerListener(this, new IrcListener(this));
    }

    @Override
    public void onDisable() {
        // Todo: Unregister and whatnot.
    }

    // Todo: Javadocs for all the following methods.

    public void addClient(ProxiedPlayer player) {
        Client client = new Client(this);

        try {
            client.connect(
                this.config.getString("server"),
                this.config.getInt("port")
            );

            ProtocolMessage webirc = new ProtocolMessage("WEBIRC");
            webirc.addArgument(this.config.getString("webirc-password"));
            webirc.addArgument(player.getName().toLowerCase());
            webirc.addArgument(player.getAddress().getHostName());
            webirc.addArgument(player.getAddress().getAddress().getHostAddress());
            client.send(webirc, true);

            ProtocolMessage user = new ProtocolMessage("USER");
            user.addArgument(player.getName().toLowerCase());
            user.addArgument("*");
            user.addArgument("*");
            user.addArgument(player.getUniqueId().toString());
            client.send(user, true);

            ProtocolMessage nick = new ProtocolMessage("NICK");
            nick.addArgument(player.getName());
            client.send(nick, true);
        } catch (IOException exception) {
            this.getLogger().info("Connection to IRC server failed!");
            exception.printStackTrace();
            return;
        }

        this.clients.put(player.getUniqueId(), client);
    }

    public boolean clientExists(ProxiedPlayer player) {
        return this.clients.containsKey(player.getUniqueId());
    }

    public boolean isInChannel(ProxiedPlayer player, String channel) {
        return false; // Todo
    }

    public void joinChannel(ProxiedPlayer player, String channel) {
        try {
            this.clients.get(player.getUniqueId()).send(new ProtocolMessage("JOIN #alacho"));
        } catch (IOException exception) {
            this.getLogger().info("Failed to join channel!");
            exception.printStackTrace();
        }
    }

    public void partChannel(ProxiedPlayer player, String channel) {
        try {
            this.clients.get(player.getUniqueId()).send(new ProtocolMessage("PART #alacho"));
        } catch (IOException exception) {
            this.getLogger().info("Failed to part channel!");
            exception.printStackTrace();
        }
    }

    public void sendMessage(ProxiedPlayer player, String channel, String message) {
        try {
            ProtocolMessage msg = new ProtocolMessage("PRIVMSG #alacho");
            msg.addArgument(message);
            this.clients.get(player.getUniqueId()).send(msg);
        } catch (IOException exception) {
            this.getLogger().info("Failed to send PRIVMSG!");
            exception.printStackTrace();
        }
    }

    public void removeClient(ProxiedPlayer player) {
        try {
            this.clients.get(player.getUniqueId()).send(new ProtocolMessage("QUIT"));
        } catch (IOException exception) {
            this.getLogger().info("Failed to send quit!");
            exception.printStackTrace();
        }

        this.clients.remove(player.getUniqueId());
    }

    public List<String> getMcChannels(String server) {
        return this.chat.getChannels(server);
    }

    public String channelMcToIrc(String channel) {
        return this.config.getString("channels." + channel);
    }

    public List<String> channelIrcToMc(String channel) {
        // Todo: Cache?
        ArrayList<String> result = new ArrayList<>();

        for (String test : this.config.getSection("channels").getKeys()) {
            if (this.config.getString("channels." + test).equals(channel))
                result.add(test);
        }

        return result;
    }
}
