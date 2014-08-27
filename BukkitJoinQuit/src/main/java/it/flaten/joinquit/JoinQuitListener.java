package it.flaten.joinquit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {
    private JoinQuit joinQuit;

    public JoinQuitListener(JoinQuit joinQuit) {
        this.joinQuit = joinQuit;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.joinQuit.displayJoinMessage(event.getPlayer()))
            event.setJoinMessage(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!this.joinQuit.displayQuitMessage(event.getPlayer()))
            event.setQuitMessage(null);
    }
}
