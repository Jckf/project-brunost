package it.flaten.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class ChatBroadcastEvent extends Event {
    private final String channel;
    private final ProxiedPlayer player;
    private final String message;

    public ChatBroadcastEvent(String channel, ProxiedPlayer player, String message) {
        this.channel = channel;
        this.player = player;
        this.message = message;
    }

    public String getChannel() {
        return this.channel;
    }

    public ProxiedPlayer getPlayer() {
        return this.player;
    }

    public String getMessage() {
        return this.message;
    }
}
