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
    private final Map<String, String> replyTo = new HashMap<>();

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

        this.getLogger().info("Registering commands...");

        this.getProxy().getPluginManager().registerCommand(this, new TellCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this));

        for (String command : this.config.getStringList("commands")) {
            this.getProxy().getPluginManager().registerCommand(this, new ChatCommand(this, command));
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getProxy().getPluginManager().unregisterCommands(this);

        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);
    }

    public Configuration getConfig() {
        return this.config;
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

    public void setReplyTo(ProxiedPlayer player, ProxiedPlayer target) {
        this.replyTo.put(target.getName(), player.getName());
    }

    public ProxiedPlayer getReplyTo(ProxiedPlayer player) {
        if (!this.replyTo.containsKey(player.getName()))
            return null;

        return this.getProxy().getPlayer(this.replyTo.get(player.getName()));
    }

    /**
     * Format a public chat message.
     *
     * @param player Player who sent the message.
     * @param message The message itself.
     * @return A BaseComponent representing the formatted message.
     */
    public BaseComponent formatMessage(ProxiedPlayer player, String message) {
        BaseComponent[] components = TextComponent.fromLegacyText(
            ChatColor.translateAlternateColorCodes(
                '&',
                this.config.getString("formats.public", "&cThe public chat is not configured properly.")
            ).replace("%name%", player.getDisplayName())
             .replace("%message%", message)
        );

        BaseComponent output = new TextComponent("");
        for (BaseComponent component : components) {
            output.addExtra(component);
        }

        return output;
    }

    /**
     * Format a private message.
     *
     * @param source Player who sent the message.
     * @param target Player who will receive the message.
     * @param message The message itself.
     * @return A BaseComponent representing the formatted message.
     */
    public BaseComponent formatMessage(ProxiedPlayer source, ProxiedPlayer target, String message) {
        BaseComponent[] components = TextComponent.fromLegacyText(
            ChatColor.translateAlternateColorCodes(
                '&',
                this.config.getString("formats.private", "&cPrivate messages are not configured properly.")
            ).replace("%source%", source.getDisplayName())
             .replace("%target%", target.getDisplayName())
             .replace("%message%", message)
        );

        BaseComponent output = new TextComponent("");
        for (BaseComponent component : components) {
            output.addExtra(component);
        }

        return output;
    }

    /**
     * Format a command message.
     *
     * @param player Player who sent the message.
     * @param command Command channel to broadcast on.
     * @param message The message itself.
     * @return A BaseComponent representing the formatted message.
     */
    public BaseComponent formatMessage(ProxiedPlayer player, String command, String message) {
        BaseComponent[] components = TextComponent.fromLegacyText(
            ChatColor.translateAlternateColorCodes(
                '&',
                this.config.getString("formats.commands." + command, "&cChannel command \"" + command + "\" is not configured properly.")
            ).replace("%name%", player.getDisplayName())
             .replace("%message%", message)
        );

        BaseComponent output = new TextComponent("");
        for (BaseComponent component : components) {
            output.addExtra(component);
        }

        return output;
    }

    /**
     * Broadcast to all servers on a given channel.
     *
     * @param channel Channel to broadcast on.
     * @param player Player who sent the message.
     * @param message The message itself.
     */
    public void channelBroadcast(String channel, ProxiedPlayer player, String message) {
        this.getProxy().getPluginManager().callEvent(new ChatBroadcastEvent(channel, player, message));

        BaseComponent formattedMessage = this.formatMessage(player, message);
        for (String serverName : this.getMembers(channel)) {
            for (ProxiedPlayer target : this.getProxy().getServerInfo(serverName).getPlayers()) {
                target.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Broadcast a message to players on a given command channel.
     *
     * @param command Command channel to broadcast on.
     * @param player Player who sent the message.
     * @param message The message itself.
     */
    public void commandBroadcast(String command, ProxiedPlayer player, String message) {
        for (ProxiedPlayer target : this.getProxy().getPlayers()) {
            if (!target.hasPermission("chat.command." + command))
                continue;

            target.sendMessage(this.formatMessage(player, command, message));
        }
    }
}
