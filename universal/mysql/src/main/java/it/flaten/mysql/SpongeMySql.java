package it.flaten.mysql;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

@Plugin(id = "mysql", name = "MySQL", version = "0.1.0-SNAPSHOT")
public class SpongeMySql {
    private MySql mySql;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.mySql = new MySql();
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        this.mySql.close();
    }

    /**
     * Get a MySQL connection pool for the given Sponge plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @return A Pool object.
     */
    public MySqlPool getPool(PluginContainer plugin) {
        return this.mySql.getPool(plugin.getName(), 0);
    }

    /**
     * Get a MySQL connection pool for the given Sponge plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @param id Pool identifier.
     * @return A Pool object.
     */
    public MySqlPool getPool(PluginContainer plugin, int id) {
        return this.mySql.getPool(plugin.getName(), id);
    }

    /**
     * Close all pools belonging to the given Sponge plugin.
     *
     * @param plugin Plugin that owns pools to close.
     */
    public void close(PluginContainer plugin) {
        this.mySql.close(plugin.getId());
    }
}
