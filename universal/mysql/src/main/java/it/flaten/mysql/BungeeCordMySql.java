package it.flaten.mysql;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCordMySql extends Plugin {
    private MySql mySql;

    @Override
    public void onEnable() {
        this.mySql = new MySql();
    }

    @Override
    public void onDisable() {
        this.mySql.close();
    }

    /**
     * Get a MySQL connection pool for the given BungeeCord plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @return A Pool object.
     */
    public MySqlPool getPool(Plugin plugin) {
        return this.mySql.getPool(plugin.getDescription().getName(), 0);
    }

    /**
     * Get a MySQL connection pool for the given BungeeCord plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @param id Pool identifier.
     * @return A Pool object.
     */
    public MySqlPool getPool(Plugin plugin, int id) {
        return this.mySql.getPool(plugin.getDescription().getName(), id);
    }

    /**
     * Close all pools belonging to the given BungeeCord plugin.
     *
     * @param plugin Plugin that owns pools to close.
     */
    public void close(Plugin plugin) {
        this.mySql.close(plugin.getDescription().getName());
    }
}
