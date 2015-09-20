package it.flaten.blockprotection;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class EventListener implements Listener {
    private BlockProtection bp;

    // Blocks that cannot be next to each other unless owned by the same person.
    private final Material[] inventoryMaterials = new Material[]{
        Material.BEACON,
        Material.BREWING_STAND,
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.DISPENSER,
        Material.DROPPER,
        Material.FURNACE,
        Material.HOPPER
    };

    // Faces with direct contact to other blocks.
    private final BlockFace[] inventoryFaces = new BlockFace[]{
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.UP,
        BlockFace.DOWN
    };

    public EventListener(BlockProtection bp) {
        this.bp = bp;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (!ArrayUtils.contains(this.inventoryMaterials, block.getType()))
            return;

        for (BlockFace face : this.inventoryFaces) {
            Block relative = block.getRelative(face);

            if (!ArrayUtils.contains(this.inventoryMaterials, relative.getType()))
                continue;

            String relativeOwner = this.bp.getBlockOwner(relative);

            if (relativeOwner == null)
                continue;

            if (relativeOwner.equals(event.getPlayer().getName()))
                continue;

            event.setCancelled(true);
            break;
        }

        if (event.isCancelled())
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place this next to someone else's container.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        String owner = this.bp.getBlockOwner(event.getBlock());

        if (owner == null)
            return;

        if (owner.equals(event.getPlayer().getName()))
            return;

        event.getPlayer().sendMessage(ChatColor.RED + "This block belongs to \"" + owner + "\".");

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Block block = null;

        // Does Bukkit really not provide an easy method to fetch the Block
        // this inventory is attached to?
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Beacon) {
            block = ((Beacon) holder).getBlock();
        } else if (holder instanceof BrewingStand) {
            block = ((BrewingStand) holder).getBlock();
        } else if (holder instanceof Chest) {
            block = ((Chest) holder).getBlock();
        } else if (holder instanceof DoubleChest) {
            block = ((DoubleChest) holder).getLocation().getBlock();
        } else if (holder instanceof Dispenser) {
            block = ((Dispenser) holder).getBlock();
        } else if (holder instanceof Dropper) {
            block = ((Dropper) holder).getBlock();
        } else if (holder instanceof Furnace) {
            block = ((Furnace) holder).getBlock();
        } else if (holder instanceof Hopper) {
            block = ((Hopper) holder).getBlock();
        }

        if (block == null)
            return;

        String owner = this.bp.getBlockOwner(block);

        if (owner == null)
            return;

        if (owner.equals(event.getPlayer().getName()))
            return;

        ((Player) event.getPlayer()).sendMessage(ChatColor.RED + "This container belongs to \"" + owner + "\".");

        event.setCancelled(true);
    }
}
