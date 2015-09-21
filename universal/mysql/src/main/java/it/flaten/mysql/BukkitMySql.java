package it.flaten.mysql;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMySql extends JavaPlugin {
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
     * Get a MySQL connection pool for the given Bukkit plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @return A Pool object.
     */
    public MySqlPool getPool(JavaPlugin plugin) {
        return this.mySql.getPool(plugin.getName(), 0);
    }

    /**
     * Get a MySQL connection pool for the given Bukkit plugin.
     *
     * @param plugin Plugin the pool will be used by.
     * @param id Pool identifier.
     * @return A Pool object.
     */
    public MySqlPool getPool(JavaPlugin plugin, int id) {
        return this.mySql.getPool(plugin.getName(), id);
    }

    /**
     * Close all pools belonging to the given Bukkit plugin.
     *
     * @param plugin Plugin that owns pools to close.
     */
    public void close(JavaPlugin plugin) {
        this.mySql.close(plugin.getName());
    }
}
