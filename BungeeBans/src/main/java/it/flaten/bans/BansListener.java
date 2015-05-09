package it.flaten.bans;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class BansListener implements Listener {
    private final Bans bans;

    public BansListener(Bans bans) {
        this.bans = bans;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        try {
            if (this.bans.isBanned(event.getPlayer())) {
                event.setCancelled(true);
                String reason = this.bans.getReason(event.getPlayer());
                this.bans.kick(event.getPlayer(), "You are banned" + (reason != null && reason.length() > 0 ? ": " + reason : "."));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();

            event.setCancelled(true);
            this.bans.kick(event.getPlayer(), "Internal error!");
        }
    }
}
