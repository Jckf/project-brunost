package it.flaten.mount;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Mount extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Registering commands...");

        this.getCommand("mount").setExecutor(new MountCommand(this));
        this.getCommand("unmount").setExecutor(new UnmountCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getCommand("unmount").setExecutor(null);
        this.getCommand("mount").setExecutor(null);
    }

    /**
     * Mount one player to another.
     *
     * @param top UUID of the player who will mount the other player.
     * @param bottom UUID of the player to be mounted.
     * @return Boolean true if the operation was successful, false otherwise.
     */
    public boolean mount(UUID top, UUID bottom) {
        return false;
    }

    /**
     * Unmount one player from another.
     *
     * @param player Player to unmount.
     * @return Boolean true of the operation was successful, false otherwise.
     */
    public boolean unmount(UUID player) {
        return false;
    }
}
