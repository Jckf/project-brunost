package it.flaten.prefixadapter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PrefixAdapterListener implements Listener {
    private final PrefixAdapter prefixAdapter;

    public PrefixAdapterListener(PrefixAdapter prefixAdapter) {
        this.prefixAdapter = prefixAdapter;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.prefixAdapter.setPlayerListName(event.getPlayer());
        
    	// Hack.
        this.prefixAdapter.getServer().getScheduler().scheduleSyncDelayedTask(this.prefixAdapter, new Runnable() {
            @Override
            public void run() {
                prefixAdapter.sendPrefix(event.getPlayer());
            }
        }, 1);
    }
    
    
}
