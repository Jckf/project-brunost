package it.flaten.permissions;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class Permissions extends Plugin {
    private final HashMap<UUID, HashMap<String, Boolean>> permissions = new HashMap<>();

    @Override
    public void onEnable() {
        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new PermissionsListener(this));

        this.getLogger().info("Registering PMCs...");

        this.getProxy().registerChannel("Permissions");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering PMCs...");

        this.getProxy().unregisterChannel("Permissions");

        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);
    }

    // TODO: Javadocs for all the following methods.

    public void setNode(UUID uuid, String node, boolean value) {
        if (!this.permissions.containsKey(uuid))
            this.permissions.put(uuid, new HashMap<String, Boolean>());

        this.permissions.get(uuid).put(node, value);
    }
    public void setNode(ProxiedPlayer player, String node, boolean value) {
        this.setNode(player.getUniqueId(), node, value);
    }

    public boolean hasNode(UUID uuid, String node) {
        return this.permissions.containsKey(uuid) && this.permissions.get(uuid).containsKey(node);
    }
    public boolean hasNode(ProxiedPlayer player, String node) {
        return this.hasNode(player.getUniqueId(), node);
    }

    public boolean getNode(UUID uuid, String node) {
        return this.permissions.containsKey(uuid) && this.permissions.get(uuid).get(node);
    }
    public boolean getNode(ProxiedPlayer player, String node) {
        return this.getNode(player.getUniqueId(), node);
    }

    public void unsetNode(UUID uuid, String node) {
        if (!this.permissions.containsKey(uuid))
            return;

        this.permissions.get(uuid).remove(node);
    }
    public void unsetNode(ProxiedPlayer player, String node) {
        this.unsetNode(player.getUniqueId(), node);
    }

    public void clearNodes(UUID uuid) {
        this.permissions.remove(uuid);
    }
    public void clearNodes(ProxiedPlayer player) {
        this.clearNodes(player.getUniqueId());
    }
}
