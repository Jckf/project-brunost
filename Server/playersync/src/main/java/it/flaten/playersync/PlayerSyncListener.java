package it.flaten.playersync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSyncListener implements Listener {
    private PlayerSync playerSync;

    public PlayerSyncListener(PlayerSync playerSync) {
        this.playerSync = playerSync;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.playerSync.loadData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.playerSync.saveData(event.getPlayer());
    }
}
