package it.flaten.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class ChatCommandEvent extends Event {
    private final ProxiedPlayer sender;
    private final String command;
    private final String message;

    public ChatCommandEvent(ProxiedPlayer sender, String command, String message) {
        this.sender = sender;
        this.command = command;
        this.message = message;
    }

    public ProxiedPlayer getSender() {
        return this.sender;
    }

    public String getCommand() {
        return this.command;
    }

    public String getMessage() {
        return this.message;
    }
}
