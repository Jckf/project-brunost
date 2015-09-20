package it.flaten.timesync;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class TimeSync extends JavaPlugin implements Runnable {
    @Override
    public void onEnable() {
        this.getLogger().info("Starting time synchronization...");

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 20, 20 * 60);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Stopping time synchronization...");

        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void run() {
        long time = ((System.currentTimeMillis() / 1000) * 20) % (20 * 60 * 20 * 8);

        for (World world : this.getServer().getWorlds()) {
            world.setFullTime(time);
        }
    }
}
