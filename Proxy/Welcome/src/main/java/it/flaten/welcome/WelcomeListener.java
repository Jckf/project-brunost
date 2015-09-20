package it.flaten.welcome;

import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class WelcomeListener implements Listener {
    private final Welcome welcome;

    public WelcomeListener(Welcome welcome) {
        this.welcome = welcome;
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPlayer().getServer() != null)
            return;

        this.welcome.sendWelcome(event.getPlayer());
    }
}
