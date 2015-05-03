package it.flaten.playerlimit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerLimitListener implements Listener {
    private final PlayerLimit playerLimit;

    public PlayerLimitListener(PlayerLimit playerLimit) {
        this.playerLimit = playerLimit;
    }

    // Todo: Maybe implement a custom event for "permissions have been set".
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("Permissions"))
            return;

        this.playerLimit.check((ProxiedPlayer) event.getReceiver());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        this.playerLimit.unCheck(event.getPlayer());
    }
}
