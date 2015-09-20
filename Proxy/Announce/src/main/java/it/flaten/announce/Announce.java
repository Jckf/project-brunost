package it.flaten.announce;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Announce extends Plugin {
    private File configFile;
    private Configuration config;
    private Map<Integer, ScheduledTask> tasks = new HashMap<>();

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

        try {
            for (Object object : this.config.getList("announcements")) {
                HashMap<String, Object> ann = (HashMap<String, Object>) object;
                this.scheduleAnnouncement(
                    this.parseMessage((String) ann.get("message")),
                    (int) ann.get("initial-delay"),
                    (int) ann.get("interval")
                );
            }
        } catch (Exception exception) {
            this.getLogger().warning("Exception while loading announcements! Probably melformed configuration.");
            exception.printStackTrace();
        }

        this.getLogger().info("Registering commands...");
        this.getProxy().getPluginManager().registerCommand(this, new AnnounceCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getProxy().getPluginManager().unregisterCommands(this);

        // Todo: Save running announcements.

        this.getLogger().info("Stopping announcements...");

        for (ScheduledTask task : this.tasks.values()) {
            task.cancel();
        }

        this.tasks = null;
    }

    /**
     * Create a BaseComponent from a text string.
     *
     * Formatting codes are allowed using the ampersand symbol.
     *
     * @param message String to parse.
     * @return BaseComponent created from the passed string.
     */
    public BaseComponent parseMessage(String message) {
        TextComponent output = new TextComponent("");

        for (BaseComponent component : TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message))) {
            output.addExtra(component);
        }

        return output;
    }

    /**
     * Get a list of active announcements.
     *
     * @return A Map of one ScheduledTask object per announcement, indexed by id.
     */
    public Map<Integer, ScheduledTask> getAnnouncements() {
        return this.tasks;
    }

    /**
     * Schedule an announcement.
     *
     * @param message Message to broadcast.
     * @param initialDelay Initial delay in seconds.
     * @param interval Interval in seconds.
     */
    public void scheduleAnnouncement(BaseComponent message, int initialDelay, int interval) {
        AnnounceTask announceTask = new AnnounceTask(this, message, interval > 0);

        ScheduledTask scheduledTask = this.getProxy().getScheduler().schedule(this, announceTask, initialDelay, interval, TimeUnit.SECONDS);

        announceTask.setScheduledTask(scheduledTask);

        this.tasks.put(scheduledTask.getId(), scheduledTask);
    }

    /**
     * Cancel an announcement.
     *
     * @param id Id of the ScheduledTask object running the announcement.
     * @return True if success. False otherwise.
     */
    public boolean cancelAnnouncement(int id) {
        if (!this.tasks.containsKey(id))
            return false;

        this.tasks.get(id).cancel();

        return this.tasks.remove(id) != null;
    }
}
