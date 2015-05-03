package it.flaten.playerlimit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class PlayerLimit extends Plugin {
    private File configFile;
    private Configuration config;

    private final HashMap<String, Integer> extraSlots = new HashMap<>();

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

        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new PlayerLimitListener(this));

        this.getLogger().info("Registering commands...");

        this.getProxy().getPluginManager().registerCommand(this, new PlayerLimitCommand(this));
    }

    @Override
    public void onDisable() {
        // Todo: Unregister...
    }

    public void check(ProxiedPlayer player) {
        String extra = this.getExtra(player);
        if (extra != null && this.getExtra(extra) < this.config.getInt("extra." + extra)) {
            this.addExtra(extra);
            return;
        }

        int sum = 0;
        for (String node : this.extraSlots.keySet()) {
            sum += this.extraSlots.get(node);
        }
        if (this.config.getInt("default") + sum > this.getProxy().getPlayers().size())
            return;

        player.disconnect(new TextComponent(this.config.getString("full-message")));
    }

    public void unCheck(ProxiedPlayer player) {
        String extra = this.getExtra(player);
        if (extra != null)
            this.removeExtra(extra);
    }

    public String getExtra(ProxiedPlayer player) {
        for (String extra : this.config.getSection("extra").getKeys()) {
            if (player.hasPermission(extra) && this.getExtra(extra) < this.config.getInt("extra." + extra))
                return extra;
        }

        return null;
    }

    public int getExtra(String node) {
        return this.extraSlots.containsKey(node) ? this.extraSlots.get(node) : 0;
    }

    public void addExtra(String node) {
        this.extraSlots.put(node, this.getExtra(node) + 1);
    }

    public void removeExtra(String node) {
        if (this.getExtra(node) <= 1) {
            this.extraSlots.remove(node);
            return;
        }

        this.extraSlots.put(node, this.extraSlots.get(node) - 1);
    }
}
