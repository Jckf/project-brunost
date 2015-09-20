package it.flaten.joinquit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinQuit extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Registering listeners...");

        this.getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        HandlerList.unregisterAll(this);
    }

    /**
     * Check if a join message should be broadcast for this player.
     *
     * @param player The player that is joining.
     * @return True if message should be displayed. False otherwise.
     */
    public boolean displayJoinMessage(Player player) {
        return player.hasPermission("joinquit.join");
    }

    /**
     * Check if a quit message should be broadcast for this player.
     *
     * @param player The player that is quitting.
     * @return True if the message should be displayed. False otherwise.
     */
    public boolean displayQuitMessage(Player player) {
        return player.hasPermission("joinquit.quit");
    }
}
