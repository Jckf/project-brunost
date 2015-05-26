package it.flaten.work;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Work extends JavaPlugin {
    private File inventoryDirectory;

    @Override
    public void onEnable() {
        this.inventoryDirectory = new File(this.getDataFolder(), "inventories");

        this.getLogger().info("Loading configuration...");

        this.saveDefaultConfig();

        this.getLogger().info("Registering commands...");

        this.getCommand("setwork").setExecutor(new SetWorkCommand(this));
        this.getCommand("work").setExecutor(new WorkCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getCommand("work").setExecutor(null);
        this.getCommand("setwork").setExecutor(null);
    }

    private File getInventoryFile(Player player) {
        this.inventoryDirectory.mkdir();

        return new File(this.inventoryDirectory, player.getUniqueId() + ".dat");
    }

    /**
     * Sets the work inventory template.
     *
     * @param inventory Inventory to use as a template.
     */
    public void setWork(PlayerInventory inventory) {
        FileConfiguration config = this.getConfig();

        config.set("template", null);

        int i = 0;
        for (ItemStack stack : inventory) {
            config.set("template." + i++, stack);
        }

        this.saveConfig();
    }

    /**
     * Check if a player is in work mode.
     *
     * @param player The player to check.
     * @return True if work is enabled. False otherwise.
     */
    public boolean isWork(Player player) {
        return this.getInventoryFile(player).exists();
    }

    /**
     * Toggle between work inventory and regular inventory.
     *
     * @param player The player whose inventory to switch.
     * @throws IOException if inventory storage fails.
     */
    public void toggleWork(Player player) throws IOException {
        if (!this.isWork(player)) {
            this.enableWork(player);
        } else {
            this.disableWork(player);
        }
    }

    private void enableWork(Player player) throws IOException {
        PlayerInventory inventory = player.getInventory();

        File inventoryFile = this.getInventoryFile(player);
        FileConfiguration file = YamlConfiguration.loadConfiguration(inventoryFile);

        file.set("inventory", null);

        int i = 0;
        for (ItemStack stack : inventory) {
            file.set("inventory." + i++, stack);
        }

        file.save(inventoryFile);

        inventory.clear();

        FileConfiguration config = this.getConfig();
        for (String key : config.getConfigurationSection("template").getKeys(false)) {
            inventory.setItem(Integer.parseInt(key), config.getItemStack("template." + key));
        }

        this.getServer().getPluginManager().callEvent(new WorkEvent(player, true));
    }

    private void disableWork(Player player) throws IOException {
        PlayerInventory inventory = player.getInventory();

        inventory.clear();

        File inventoryFile = this.getInventoryFile(player);
        FileConfiguration file = YamlConfiguration.loadConfiguration(inventoryFile);
        for (String key : file.getConfigurationSection("inventory").getKeys(false)) {
            inventory.setItem(Integer.parseInt(key), file.getItemStack("inventory." + key));
        }

        if (!inventoryFile.delete()) {
            // Todo: Handle this very, very, VERY well!
            inventory.clear();

            throw new IOException("Failed to delete inventory file: " + inventoryFile);
        }

        this.getServer().getPluginManager().callEvent(new WorkEvent(player, false));
    }
}
