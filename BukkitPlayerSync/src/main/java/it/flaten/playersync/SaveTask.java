package it.flaten.playersync;

import java.util.UUID;

public class SaveTask implements Runnable {
    private final PlayerSync playerSync;
    private final UUID uuid;

    public SaveTask(PlayerSync playerSync, UUID uuid) {
        this.playerSync = playerSync;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        if (this.playerSync.getConfig().getBoolean("sync.inventory"))
            this.saveInventory();

        if (this.playerSync.getConfig().getBoolean("sync.armor"))
            this.saveArmor();

        if (this.playerSync.getConfig().getBoolean("sync.health"))
            this.saveHealth();

        if (this.playerSync.getConfig().getBoolean("sync.exp"))
            this.saveExp();

        if (this.playerSync.getConfig().getBoolean("sync.hunger"))
            this.saveHunger();

        if (this.playerSync.getConfig().getBoolean("sync.effects"))
            this.saveEffects();
    }

    private void saveInventory() {

    }

    private void saveArmor() {

    }

    private void saveHealth() {

    }

    private void saveExp() {

    }

    private void saveHunger() {

    }

    private void saveEffects() {

    }
}
