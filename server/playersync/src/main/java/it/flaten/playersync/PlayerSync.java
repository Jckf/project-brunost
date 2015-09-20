package it.flaten.playersync;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.UUID;

public class PlayerSync extends JavaPlugin {
    private Connection db;

    private PreparedStatement testSelect;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading configuration...");

        this.saveDefaultConfig();

        this.getLogger().info("Testing database connection...");

        if (this.getDb() == null) {
            this.getLogger().warning("Database connection failed! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Registering event handlers...");

        this.getServer().getPluginManager().registerEvents(new PlayerSyncListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Cleaning up database connection...");

        try {
            if (this.testSelect != null)
                this.testSelect.close();
        } catch (SQLException ignored) { }

        try {
            if (this.db != null)
                this.db.close();
        } catch (SQLException ignored) { }
    }

    /**
     * Initiate a database connection.
     *
     * An existing connection will be returned if available.
     *
     * @return A valid database connection, or null if the database is unavailable.
     */
    private Connection getDb() {
        if (this.db != null) {
            try {
                if (this.db.isClosed())
                    throw new SQLException("An existing connection was closed.");

                ResultSet result = this.testSelect.executeQuery();
                if (result.next())
                    return this.db;
            } catch (SQLException ignored) { }

            // It wasn't null, but we couldn't use it. Be gone, evildoer!
            this.db = null;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.db = DriverManager.getConnection(
                    "jdbc:mysql://" + this.getConfig().getString("mysql.hostname", "localhost") + ":" + this.getConfig().getInt("mysql.port", 3306) + "/" + this.getConfig().getString("mysql.schema", "bank"),
                    this.getConfig().getString("mysql.username", "root"),
                    this.getConfig().getString("mysql.password", "")
            );

            // New connection means new statements.
            this.testSelect = this.db.prepareStatement("SELECT 1");

            // Todo: Verify table existence, and create if needed.
        } catch (ClassNotFoundException exception) {
            this.db = null;
            this.getLogger().warning("ClassNotFoundException while connecting to database!");
        } catch (SQLException exception) {
            this.db = null;
            this.getLogger().warning("SQLException while connecting to database!");
            exception.printStackTrace();
        }

        return this.db;
    }

    public void loadData(Player player) {
        this.loadData(player.getUniqueId());
    }
    public void loadData(UUID uuid) {
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new LoadTask(this, uuid), this.getConfig().getInt("load_delay"));
    }

    public void saveData(Player player) {
        this.saveData(player.getUniqueId());
    }
    public void saveData(UUID uuid) {
        new SaveTask(this, uuid).run();
    }
}
