package it.flaten.blockprotection;

import me.botsko.prism.Prism;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockProtection extends JavaPlugin {
    private Prism prism;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading Prism API...");

        this.prism = ((Prism) this.getServer().getPluginManager().getPlugin("Prism"));

        this.getLogger().info("Registering event handlers...");

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering event handlers...");

        HandlerList.unregisterAll(this);
    }
}
