package it.flaten.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
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
        if (event.isCommand() || event.isCancelled())
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

        for (String channel : channels) {
            this.chat.channelBroadcast(channel, player, event.getMessage());
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        if (!in.readUTF().equals("Prefix"))
            return;

        // Thug life.
        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();

        String name = in.readUTF() + player.getName();
        player.setDisplayName(name.length() > 16 ? name.substring(0, 16) : name);
    }
}
