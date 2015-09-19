package it.flaten.playersync;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LoadTask extends BukkitRunnable implements Runnable {
    private final PlayerSync playerSync;
    private final UUID uuid;

    public LoadTask(PlayerSync playerSync, UUID uuid) {
        this.playerSync = playerSync;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        if (this.playerSync.getConfig().getBoolean("sync.inventory"))
            this.loadInventory();

        if (this.playerSync.getConfig().getBoolean("sync.armor"))
            this.loadArmor();

        if (this.playerSync.getConfig().getBoolean("sync.health"))
            this.loadHealth();

        if (this.playerSync.getConfig().getBoolean("sync.exp"))
            this.loadExp();

        if (this.playerSync.getConfig().getBoolean("sync.hunger"))
            this.loadHunger();

        if (this.playerSync.getConfig().getBoolean("sync.effects"))
            this.loadEffects();
    }

    private void loadInventory() {

    }

    private void loadArmor() {

    }

    private void loadHealth() {

    }

    private void loadExp() {

    }

    private void loadHunger() {

    }

    private void loadEffects() {

    }
}
