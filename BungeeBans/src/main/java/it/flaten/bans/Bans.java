package it.flaten.bans;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bans extends Plugin {
    private File configFile;
    private Configuration config;

    private Connection db;

    private PreparedStatement testSelect;
    private PreparedStatement insertBan;
    private PreparedStatement selectBan;
    private PreparedStatement deleteBan;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading configuration...");

        this.configFile = new File(this.getDataFolder(), "config.yml");

        if (!this.configFile.exists()) {
            this.getDataFolder().mkdir();

            try {
                Files.copy(this.getResourceAsStream("config.yml"), this.configFile.toPath());
            } catch (IOException exception) {
                this.getLogger().severe("Failed to copy default configuration.");
                exception.printStackTrace();
            }
        }

        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
        } catch (IOException exception) {
            this.getLogger().severe("Failed to load config.yml. This plugin will not do anything.");
            return;
        }

        this.getLogger().info("Testing database connection...");

        if (this.getDb() == null) {
            this.getLogger().warning("Database connection failed! Disabling plugin...");
            this.onDisable();
            return;
        }

        this.getLogger().info("Registering listeners...");

        this.getProxy().getPluginManager().registerListener(this, new BansListener(this));

        this.getLogger().info("Registering commands...");

        this.getProxy().getPluginManager().registerCommand(this, new KickCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new HistoryCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getProxy().getPluginManager().unregisterCommands(this);

        this.getLogger().info("Unregistering listeners...");

        this.getProxy().getPluginManager().unregisterListeners(this);

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
                "jdbc:mysql://" + this.config.getString("mysql.hostname", "localhost") + ":" + this.config.getInt("mysql.port", 3306) + "/" + this.config.getString("mysql.schema", "bans"),
                this.config.getString("mysql.username", "root"),
                this.config.getString("mysql.password", "")
            );

            // New connection means new statements.
            this.testSelect = this.db.prepareStatement("SELECT 1");
            this.insertBan = this.db.prepareStatement("INSERT INTO bans (uuid, `by`, timestamp, reason) VALUES (?, ?, ?, ?)");
            this.selectBan = this.db.prepareStatement("SELECT id,reason FROM bans WHERE uuid=?");
            this.deleteBan = this.db.prepareStatement("DELETE FROM bans WHERE id=?");

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

    // Todo: Javadocs...

    public void kick(ProxiedPlayer player, String reason) {
        if (reason == null) {
            player.disconnect();
        } else {
            player.disconnect(new TextComponent(reason));
        }
    }

    public void ban(UUID uuid, UUID by, String reason) throws SQLException {
        if (this.isBanned(uuid))
            throw new IllegalArgumentException("The player is already banned!");

        this.getDb();

        this.insertBan.setString(1, uuid.toString());
        this.insertBan.setString(2, by == null ? null : by.toString());
        this.insertBan.setInt(3, (int) (System.currentTimeMillis() / 1000L));
        this.insertBan.setString(4, reason);

        if (this.insertBan.executeUpdate() != 1)
            throw new SQLException("Unexpected number of affected rows!");

        this.insertBan.clearParameters();
    }
    public void ban(ProxiedPlayer player, ProxiedPlayer by, String reason) throws SQLException {
        this.ban(player.getUniqueId(), by == null ? null : by.getUniqueId(), reason);
    }

    public void unban(UUID uuid, String reason) throws SQLException {
        this.getDb();

        this.selectBan.setString(1, uuid.toString());

        ResultSet result = this.selectBan.executeQuery();

        this.selectBan.clearParameters();

        if (!result.next())
            return;

        this.deleteBan.setInt(1, result.getInt(1));

        if (this.deleteBan.executeUpdate() != 1)
            throw new SQLException("Unexpected number of affected rows!");

        this.deleteBan.clearParameters();
    }
    public void unban(ProxiedPlayer player, String reason) throws SQLException {
        this.unban(player.getUniqueId(), reason);
    }

    public boolean isBanned(UUID uuid) throws SQLException {
        this.getDb();

        this.selectBan.setString(1, uuid.toString());

        ResultSet result = this.selectBan.executeQuery();

        this.selectBan.clearParameters();

        return result.next();
    }
    public boolean isBanned(ProxiedPlayer player) throws SQLException {
        return this.isBanned(player.getUniqueId());
    }

    public String getReason(UUID uuid) throws SQLException {
        this.getDb();

        this.selectBan.setString(1, uuid.toString());

        ResultSet result = this.selectBan.executeQuery();

        this.selectBan.clearParameters();

        if (!result.next())
            return null;

        return result.getString(2);
    }
    public String getReason(ProxiedPlayer player) throws SQLException {
        return this.getReason(player.getUniqueId());
    }

    public List<String> getHistory(UUID uuid) {
        return new ArrayList<>();
    }
    public List<String> getHistory(ProxiedPlayer player) {
        return this.getHistory(player.getUniqueId());
    }

    public UUID getUuid(String name) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            connection.connect();

            JsonObject root = new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();

            String uuid = root.get("id").getAsString();
            return new UUID(
                new BigInteger(uuid.substring(0, 16), 16).longValue(),
                new BigInteger(uuid.substring(16), 16).longValue()
            );
        } catch (IOException exception) {
            this.getLogger().warning("Failed to fetch UUID!");
            exception.printStackTrace();
        }

        return null;
    }
}
