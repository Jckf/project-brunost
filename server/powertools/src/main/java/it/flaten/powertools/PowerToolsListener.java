package it.flaten.powertools;

import it.flaten.work.WorkEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PowerToolsListener implements Listener {
    private final PowerTools powerTools;

    public PowerToolsListener(PowerTools powerTools) {
        this.powerTools = powerTools;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!this.powerTools.getWork().isWork(event.getPlayer()))
            return;

        this.powerTools.setStatus(event.getPlayer(), true);

        event.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onWork(WorkEvent event) {
        this.powerTools.setStatus(event.getPlayer(), event.getInWork());

        event.getPlayer().setAllowFlight(event.getInWork());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.powerTools.setStatus(event.getPlayer(), false);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        if (this.powerTools.getStatus((Player) event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        if (player.getFoodLevel() < event.getFoodLevel())
            return;

        if (this.powerTools.getStatus(player))
            event.setCancelled(true);
    }
}
