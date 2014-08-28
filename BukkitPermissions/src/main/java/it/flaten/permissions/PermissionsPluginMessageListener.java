package it.flaten.permissions;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.Map;

public class PermissionsPluginMessageListener implements PluginMessageListener {
    private final Permissions permissions;

    public PermissionsPluginMessageListener(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(data);

        String subChannel = in.readUTF();

        if (!subChannel.equals("Permissions"))
            return;

        Map<String, Boolean> permissions = new HashMap<>();

        int nodes = in.readInt();
        for (int i = 0; i < nodes; i++) {
            String node = in.readUTF();
            boolean value = in.readBoolean();

            permissions.put(node, value);
        }

        this.permissions.setNodes(player, permissions);
    }
}
