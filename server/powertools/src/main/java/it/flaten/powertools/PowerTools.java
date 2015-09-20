package it.flaten.powertools;

import it.flaten.work.Work;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PowerTools extends JavaPlugin {
    private Work work;
    private final ArrayList<UUID> players = new ArrayList<>();

    @Override
    public void onEnable() {
        this.work = (Work) this.getServer().getPluginManager().getPlugin("Work");

        this.getServer().getPluginManager().registerEvents(new PowerToolsListener(this), this);
    }

    @Override
    public void onDisable() {

    }

    public Work getWork() {
        return this.work;
    }

    public void setStatus(UUID uuid, boolean status) {
        if (status) {
            this.players.add(uuid);
        } else {
            this.players.remove(uuid);
        }
    }
    public void setStatus(Player player, boolean status) {
        this.setStatus(player.getUniqueId(), status);
    }

    public boolean getStatus(UUID uuid) {
        return this.players.contains(uuid);
    }
    public boolean getStatus(Player player) {
        return this.getStatus(player.getUniqueId());
    }
}
