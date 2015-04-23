package it.flaten.permissions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class Permissions extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Binding to PMC...");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "Permissions");

        this.getLogger().info("Registering event handlers...");

        this.getServer().getPluginManager().registerEvents(new PermissionsListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering event handlers...");

        HandlerList.unregisterAll(this);

        this.getLogger().info("Unbinding from PMC...");

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public void setPermissions(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();

        out.writeUTF("Set");
        out.writeInt(perms.size());
        for (PermissionAttachmentInfo perm : perms) {
            out.writeUTF(perm.getPermission());
            out.writeBoolean(perm.getValue());
        }

        player.sendPluginMessage(this, "Permissions", out.toByteArray());
    }
}
