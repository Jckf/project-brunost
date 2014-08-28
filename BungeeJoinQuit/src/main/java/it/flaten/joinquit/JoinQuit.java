package it.flaten.joinquit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class JoinQuit extends Plugin {
    private File configFile;
    private Configuration config;

    private Map<UUID, String> locations;

    @Override
    public void onEnable() {
        this.locations = new HashMap<>();

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
            this.getLogger().severe("Failed to load config.yml.");
            exception.printStackTrace();
        }

        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new JoinQuitListener(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);

        this.locations = null;
    }

    /**
     * Get a list of all configured groups.
     *
     * @return An ArrayList with the name of all the groups.
     */
    public List<String> getGroups() {
        return new ArrayList(this.config.getSection("groups").getKeys());
    }

    /**
     * Get the name of the group the given server belongs to.
     *
     * @param server The server whose group we're looking for.
     * @return Name of the group the given server belongs to. Null if no match is found.
     */
    public String getGroup(String server) {
        for (String group : this.getGroups()) {
            List<String> servers = this.getServers(group);

            if (servers.contains(server) || servers.contains("*"))
                return group;
        }

        return null;
    }

    /**
     * Get a list of all servers in a specific group.
     *
     * @param group The group to use.
     * @return A List of server names.
     */
    public List<String> getServers(String group) {
        List<String> servers = this.config.getStringList("groups." + group);

        if (servers.contains("*")) {
            servers.clear();

            for (ServerInfo server : this.getProxy().getServers().values())
                servers.add(server.getName());
        }

        return servers;
    }

    public BaseComponent getMessage(String event, String player) {
        BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes(
            '&',
            this.config.getString("messages." + event)
                .replace("%name%", player)
        ));

        BaseComponent output = new TextComponent("");
        for (BaseComponent component : components)
            output.addExtra(component);

        return output;
    }

    /**
     * Broadcast a message to all players on servers in a given group.
     *
     * @param group Group to broadcast to.
     * @param message Message to broadcast.
     */
    public void broadcast(String group, BaseComponent message) {
        for (String server : this.getServers(group))
            for (ProxiedPlayer player : this.getProxy().getServerInfo(server).getPlayers())
                player.sendMessage(message);
    }

    public void setLocation(UUID uuid, String server) {
        if (server == null) {
            this.locations.remove(uuid);
            return;
        }

        this.locations.put(uuid, server);
    }

    public String getLocation(UUID uuid) {
        return this.locations.get(uuid);
    }
}
