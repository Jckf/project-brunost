package it.flaten.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.Map;

public class Chat extends Plugin {
    private File configFile;
    private Configuration config;
    private final Map<String, List<String>> channels = new HashMap<>();

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

        for (String channel : this.config.getSection("channels").getKeys()) {
            for (String server : this.config.getStringList("channels." + channel)) {
                this.joinChannel(server, channel);
            }
        }

        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new ChatListener(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);
    }

    /**
     * Add a server to a channel.
     *
     * @param server Server to add.
     * @param channel Channel to join.
     */
    public void joinChannel(String server, String channel) {
        if (!this.channels.containsKey(channel))
            this.channels.put(channel, new ArrayList<String>());

        this.channels.get(channel).add(server);
    }

    /**
     * Remove a server from a channel.
     *
     * @param server Server to remove.
     * @param channel Channel to part.
     */
    public void partChannel(String server, String channel) {
        if (!this.channels.containsKey(channel))
            return;

        this.channels.get(channel).remove(server);

        if (this.channels.get(channel).isEmpty())
            this.channels.remove(channel);
    }

    /**
     * Get all channel names.
     *
     * @return A List of all channel names.
     */
    public List<String> listChannels() {
        List<String> output = new ArrayList<>();

        output.addAll(this.channels.keySet());

        return output;
    }

    /**
     * Get all channels this server is currently in.
     *
     * @param server Server to check.
     * @return A List of channel names.
     */
    public List<String> getChannels(String server) {
        List<String> output = new ArrayList<>();

        for (String channel : this.channels.keySet()) {
            if (this.channels.get(channel).contains(server))
                output.add(channel);
        }

        return output.isEmpty() ? null : output;
    }

    /**
     * Get all members of a channel.
     *
     * @param channel Name of the channel.
     * @return A List of server names.
     */
    public List<String> getMembers(String channel) {
        return this.channels.get(channel);
    }

    public BaseComponent formatMessage(ProxiedPlayer player, String message) {
        // Todo: Lookup permissions to add prefixes.

        TextComponent nameComponent = new TextComponent(player.getName() + ": ");
        nameComponent.setColor(ChatColor.WHITE);
        nameComponent.setBold(true);

        TextComponent messageComponent = new TextComponent(message);
        messageComponent.setColor(ChatColor.GRAY);

        TextComponent output = new TextComponent("");
        output.addExtra(nameComponent);
        output.addExtra(messageComponent);

        return output;
    }
}
