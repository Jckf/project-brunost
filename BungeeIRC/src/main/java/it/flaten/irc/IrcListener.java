package it.flaten.irc;

import it.flaten.chat.ChatBroadcastEvent;
import it.flaten.chat.ChatCommandEvent;
import it.flaten.chat.ChatPrivateEvent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class IrcListener implements Listener {
    private final Irc irc;

    public IrcListener(Irc irc) {
        this.irc = irc;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.getPlayer().getServer() == null) {
            // When a player connects to the proxy, add a client.
            this.irc.addClient(event.getPlayer());
        } else {
            // Todo: When a player switches servers, part channels the target server isn't in.
        }

        // Join all channels the server we're connecting to is in.
        // Todo: Redo this in conjunction with the above todo.
        for (String channel : this.irc.getMcChannels(event.getTarget().getName())) {
            this.irc.joinChannel(event.getPlayer(), this.irc.channelMcToIrc(channel));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatBroadcastEvent event) {
        for (String channel : this.irc.getMcChannels(event.getPlayer().getServer().getInfo().getName())) {
            this.irc.sendMessage(event.getPlayer(), this.irc.channelMcToIrc(channel), event.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatCommand(ChatCommandEvent event) {
        if (!this.irc.isInChannel(event.getSender(), this.irc.channelCommandToIrc(event.getCommand())))
            this.irc.joinChannel(event.getSender(), this.irc.channelCommandToIrc(event.getCommand()));

        this.irc.sendMessage(event.getSender(), this.irc.channelCommandToIrc(event.getCommand()), event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatPrivate(ChatPrivateEvent event) {
        if (!this.irc.isInChannel(event.getSender(), this.irc.getTellChannel()))
            this.irc.joinChannel(event.getSender(), this.irc.getTellChannel());

        this.irc.sendMessage(event.getSender(), this.irc.getTellChannel(), " -> " + event.getReceiver().getName() + ": " + event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        this.irc.removeClient(event.getPlayer());
    }
}
