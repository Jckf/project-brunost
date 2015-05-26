package it.flaten.work;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorkEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final boolean inWork;

    public WorkEvent(Player player, boolean inWork) {
        this.player = player;
        this.inWork = inWork;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean getInWork() {
        return this.inWork;
    }

    @Override
    public HandlerList getHandlers() {
        return this.handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
