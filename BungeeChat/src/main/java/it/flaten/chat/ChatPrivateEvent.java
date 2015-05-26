package it.flaten.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class ChatPrivateEvent extends Event {
    private final ProxiedPlayer sender;
    private final ProxiedPlayer receiver;
    private final String message;

    public ChatPrivateEvent(ProxiedPlayer sender, ProxiedPlayer receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public ProxiedPlayer getSender() {
        return this.sender;
    }

    public ProxiedPlayer getReceiver() {
        return this.receiver;
    }

    public String getMessage() {
        return this.message;
    }
}
