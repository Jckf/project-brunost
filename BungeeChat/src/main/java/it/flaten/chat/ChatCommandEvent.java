package it.flaten.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class ChatCommandEvent extends Event {
    private final String command;
    private final ProxiedPlayer sender;
    private final ProxiedPlayer receiver;
    private final String message;

    public ChatCommandEvent(String command, ProxiedPlayer sender, ProxiedPlayer receiver, String message) {
        this.command = command;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getCommand() {
        return this.command;
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
