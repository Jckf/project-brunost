package it.flaten.antispam;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class AntiSpamListener implements Listener {
    private final AntiSpam antiSpam;

    public AntiSpamListener(AntiSpam antiSpam) {
        this.antiSpam = antiSpam;
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        if (this.antiSpam.isMuted(event.getPlayer())) {
            TextComponent warning = new TextComponent("You are muted.");
            warning.setColor(ChatColor.RED);

            event.getPlayer().sendMessage(warning);
        }
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        event.setCancelled(!this.antiSpam.handleMessage((ProxiedPlayer) event.getSender(), event.getMessage()));
    }

    @EventHandler
    public void onLogout(PlayerDisconnectEvent event) {
        this.antiSpam.clearLog(event.getPlayer());
    }
}
