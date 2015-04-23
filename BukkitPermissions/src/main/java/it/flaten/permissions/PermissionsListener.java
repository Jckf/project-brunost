package it.flaten.permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PermissionsListener implements Listener {
    private Permissions permissions;

    public PermissionsListener(Permissions permissions) {
        this.permissions = permissions;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        // Hack.
        this.permissions.getServer().getScheduler().scheduleSyncDelayedTask(this.permissions, new Runnable() {
            @Override
            public void run() {
                permissions.setPermissions(event.getPlayer());
            }
        }, 1);
    }
}
