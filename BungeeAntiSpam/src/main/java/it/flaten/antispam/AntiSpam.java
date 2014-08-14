package it.flaten.antispam;

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
import java.util.*;

public class AntiSpam extends Plugin {
    private File configFile;
    private Configuration config;
    private Map<UUID, Map<Long, String>> log = new HashMap<>();
    private List<UUID> mutes = new ArrayList<>();

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

        this.getProxy().getPluginManager().registerListener(this, new AntiSpamListener(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);
    }

    /**
     * Mute a given player.
     *
     * @param player The player to mute.
     */
    public void mute(ProxiedPlayer player) {
        this.mutes.add(player.getUniqueId());
    }

    /**
     * Check if a given player is muted.
     *
     * @return True if muted. False otherwise.
     */
    public boolean isMuted(ProxiedPlayer player) {
        return this.mutes.contains(player.getUniqueId());
    }

    /**
     * Unmute a muted player.
     *
     * @param player The player to unmute.
     */
    public void unmute(ProxiedPlayer player) {
        this.mutes.remove(player.getUniqueId());
    }

    /**
     * Verify if a message is spam or not, and handle the situation accordingly.
     *
     * @param player Player that sent the message.
     * @param message The message itself.
     * @return True if the message should be allowed. False if it is spam.
     */
    public boolean handleMessage(ProxiedPlayer player, String message) {
        // Make sure we have somewhere to store chat history for this player.
        if (!this.log.containsKey(player.getUniqueId()))
            this.log.put(player.getUniqueId(), new HashMap<Long, String>());

        // Fetch the chat history for this player.
        Map<Long, String> playerLog = this.log.get(player.getUniqueId());

        // Some commonly used numbers.
        long currentTimeMillis = System.currentTimeMillis();
        long oldest = playerLog.isEmpty() ? 0 : Collections.min(playerLog.keySet());
        long previous = playerLog.isEmpty() ? currentTimeMillis : Collections.max(playerLog.keySet());

        // Is muted?
        if (this.mutes.contains(player.getUniqueId())) {
            // Yes. Time to unmute?
            if ((int) ((currentTimeMillis - previous) / 1000L) <= this.config.getInt("mute-timeout")) {
                // Nope.
                return false;
            }

            this.unmute(player);
        }

        // Store this message.
        playerLog.put(currentTimeMillis, message);

        // Count repetitions of this message in the history.
        int repeats = -1;
        for (long ts : playerLog.keySet()) {
            if (playerLog.get(ts).equalsIgnoreCase(message) && ((int) ((currentTimeMillis - ts) / 1000L)) < this.config.getInt("repeat-delay"))
                repeats++;
        }

        // Was the repetition count higher than the configured threshold?
        if (repeats >= this.config.getInt("repeat-max")) {
            // Yup. Mute!
            TextComponent warning = new TextComponent("You have been muted for repetition spam.");
            warning.setColor(ChatColor.RED);
            player.sendMessage(warning);

            this.mute(player);
            return false;
        }

        // Check if the amount of time that has passed sinces the oldest message in the history is less than the configured threshold.
        if (playerLog.size() >= this.config.getInt("history") && (int) ((currentTimeMillis - oldest) / 1000L) < this.config.getInt("quick-time")) {
            // It is :( Mute!
            TextComponent warning = new TextComponent("You have been muted for chatting too quickly.");
            warning.setColor(ChatColor.RED);
            player.sendMessage(warning);

            this.mute(player);
            return false;
        }

        // Trunkate history.
        while (playerLog.size() > this.config.getInt("history"))
            playerLog.remove(Collections.min(playerLog.keySet()));

        // Default to allowing chat.
        return true;
    }

    public void clearLog(ProxiedPlayer player) {
        this.log.remove(player.getUniqueId());
    }
}
