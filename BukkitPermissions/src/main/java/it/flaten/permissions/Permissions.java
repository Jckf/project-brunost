package it.flaten.permissions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class Permissions extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Binding to BungeeCord PMC...");

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PermissionsPluginMessageListener(this));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.getLogger().info("Registering event handlers...");

        this.getServer().getPluginManager().registerEvents(new PermissionsListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering event handlers...");

        HandlerList.unregisterAll(this);

        this.getLogger().info("Unbinding from BungeeCord PMC...");

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    public void requestNodes(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Permissions");
        out.writeUTF(player.getUniqueId().toString());

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void setNodes(Player player, Map<String, Boolean> permissions) {
        for (String node : permissions.keySet()) {
            player.addAttachment(this, node, permissions.get(node));
        }
    }
}
