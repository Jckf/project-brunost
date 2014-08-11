package it.flaten.blockprotection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class EventListener implements Listener {
    private BlockProtection bp;

    public EventListener(BlockProtection bp) {
        this.bp = bp;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

    }
}
