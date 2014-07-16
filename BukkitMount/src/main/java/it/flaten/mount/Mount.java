package it.flaten.mount;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Mount extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Registering commands...");

        this.getCommand("mount").setExecutor(new MountCommand(this));
        this.getCommand("unmount").setExecutor(new UnmountCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getCommand("unmount").setExecutor(null);
        this.getCommand("mount").setExecutor(null);
    }

    /**
     * Mount one player to another.
     *
     * @param top UUID of the player who will mount the other player.
     * @param bottom UUID of the player to be mounted.
     * @throws PlayerOfflineException if either of the given players are offline.
     * @throws OccupiedException if the bottom player already has a passenger.
     */
    public void mount(UUID top, UUID bottom) throws PlayerOfflineException, OccupiedException {
        Player pTop = this.getServer().getPlayer(top);

        if (pTop == null)
            throw new PlayerOfflineException();

        Player pBottom = this.getServer().getPlayer(bottom);

        if (pBottom == null)
            throw new PlayerOfflineException();

        if (pBottom.getPassenger() != null)
            throw new OccupiedException();

        pBottom.setPassenger(pTop);
    }

    /**
     * Unmount one player from another.
     *
     * @param player Player to unmount.
     * @throws PlayerOfflineException if the given player is offline.
     * @throws NoVehicleException if the given player is not in any vehicle.
     */
    public void unmount(UUID player) throws PlayerOfflineException, NoVehicleException {
        Player pPlayer = this.getServer().getPlayer(player);

        if (pPlayer == null)
            throw new PlayerOfflineException();

        if (pPlayer.getVehicle() == null)
            throw new NoVehicleException();

        if (!pPlayer.leaveVehicle())
            throw new UnknownError("leaveVehicle() returned false!");
    }
}
