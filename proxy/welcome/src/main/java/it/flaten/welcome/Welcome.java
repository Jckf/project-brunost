package it.flaten.welcome;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Welcome extends Plugin {
    private File configFile;
    private Configuration config;

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
            this.getLogger().severe("Failed to load config.yml.");
            exception.printStackTrace();
        }

        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new WelcomeListener(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);
    }

    public void sendWelcome(ProxiedPlayer player) {
        player.sendTitle(this.getProxy().createTitle().reset());

        Title title = this.getProxy().createTitle();

        title.title(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', this.config.getString("title"))));
        title.subTitle(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', this.config.getString("sub-title"))));

        player.sendTitle(title);
    }
}
