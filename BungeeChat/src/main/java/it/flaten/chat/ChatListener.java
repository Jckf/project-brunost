package it.flaten.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class ChatListener implements Listener {
    private final Chat chat;

    public ChatListener(Chat chat) {
        this.chat = chat;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (event.isCommand())
            return;

        // I am not sure that sender and receiver can be anything other than ProxiedPlayer
        // and Server, but the code allows for it, so better be sure.
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        if (!(event.getReceiver() instanceof Server))
            return;

        event.setCancelled(true);

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        Server server = (Server) event.getReceiver();

        List<String> channels = this.chat.getChannels(server.getInfo().getName());

        if (channels == null) {
            this.chat.getLogger().warning("Got Chat message targeted at a server that has not joined any channels (" + server.getInfo().getName() + ").");
            return;
        }

        // Are woo looping too much here?
        for (String channel : channels) {
            // Todo: Fire an event here to let other plugins know we are going to send a message to a channel.

            for (String serverName : this.chat.getMembers(channel)) {
                for (ProxiedPlayer target : this.chat.getProxy().getServerInfo(serverName).getPlayers()) {
                    target.sendMessage(this.chat.formatMessage(player, event.getMessage()));
                }
            }
        }
    }
}
