package it.flaten.permissions;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PermissionsListener implements Listener {
    private final Permissions permissions;

    public PermissionsListener(Permissions permissions) {
        this.permissions = permissions;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        this.permissions.getLogger().info("PM on " + event.getTag());

        if (!event.getTag().equals("Permissions"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        if (!in.readUTF().equals("Set"))
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();

        this.permissions.clearNodes(player);

        int nodes = in.readInt();
        for (int i = 0; i < nodes; i++) {
            this.permissions.setNode(player, in.readUTF(), in.readBoolean());
        }
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!this.permissions.hasNode(player, event.getPermission()))
            return;

        event.setHasPermission(this.permissions.getNode(player, event.getPermission()));
    }

    @EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {
        this.permissions.clearNodes(event.getPlayer());
    }
}
