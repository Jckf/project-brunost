package it.flaten.joinquit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinQuitListener implements Listener {
    private JoinQuit joinQuit;

    public JoinQuitListener(JoinQuit joinQuit) {
        this.joinQuit = joinQuit;
    }

    // Todo: Use ServerConnectedEvent instead of custom tracking.

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        String from = this.joinQuit.getLocation(player.getUniqueId());
        String to = player.getServer().getInfo().getName();

        String fromGroup = from != null ? fromGroup = this.joinQuit.getGroup(from) : null;
        String toGroup = this.joinQuit.getGroup(to);

        if (fromGroup != null && !fromGroup.equals(toGroup))
            this.joinQuit.broadcast(fromGroup, this.joinQuit.getMessage("quit", player.getDisplayName()));

        if (fromGroup == null || !fromGroup.equals(toGroup))
            this.joinQuit.broadcast(toGroup, this.joinQuit.getMessage("join", player.getDisplayName()));

        this.joinQuit.setLocation(player.getUniqueId(), to);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (event.getPlayer().getServer() == null)
            return;

        String fromGroup = this.joinQuit.getGroup(event.getPlayer().getServer().getInfo().getName());

        if (fromGroup != null)
            this.joinQuit.broadcast(fromGroup, this.joinQuit.getMessage("quit", event.getPlayer().getDisplayName()));

        this.joinQuit.setLocation(event.getPlayer().getUniqueId(), null);
    }
}
