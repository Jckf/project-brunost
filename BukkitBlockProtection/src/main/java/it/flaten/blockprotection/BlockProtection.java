package it.flaten.blockprotection;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import org.bukkit.block.Block;
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

    /**
     * Get the name of the given Block's owner.
     *
     * @param block The Block to check.
     * @return The owner's name.
     */
    public String getBlockOwner(Block block) {
        QueryParameters params = new QueryParameters();
        params.setSpecificBlockLocation(block.getLocation());
        params.addActionType("block-place");
        params.setLimit(1);

        ActionsQuery query = new ActionsQuery(this.prism);
        QueryResult result = query.lookup(params);

        if (result.getActionResults().isEmpty())
            return null;

        return result.getActionResults().get(0).getPlayerName();
    }
}
