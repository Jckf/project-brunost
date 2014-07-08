package it.flaten.bank;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class Bank extends JavaPlugin {
    private Connection db;

    private PreparedStatement insertTransaction;

    private PreparedStatement selectBalance;
    private PreparedStatement insertBalance;
    private PreparedStatement updateBalance;

    private Material currency;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading configuration...");

        this.saveDefaultConfig();

        this.currency = Material.valueOf(this.getConfig().getString("bank.currency", "GOLD_INGOT"));
        if (currency == null) {
            this.getLogger().warning("Invalid currency in config.yml! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Testing database connection...");

        if (this.getDb() == null) {
            this.getLogger().warning("Database connection failed! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Registering commands...");

        this.getCommand("bank").setExecutor(new BankCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering commands...");

        this.getCommand("bank").setExecutor(null);

        this.getLogger().info("Cleaning up database connection...");

        try {
            if (this.updateBalance != null)
                this.updateBalance.close();
        } catch (SQLException exception) { }

        try {
            if (this.insertBalance != null)
                this.insertBalance.close();
        } catch (SQLException exception) { }

        try {
            if (this.selectBalance != null)
                this.selectBalance.close();
        } catch (SQLException exception) { }

        try {
            if (this.insertTransaction != null)
                this.insertTransaction.close();
        } catch (SQLException exception) { }

        try {
            if (this.db != null)
                this.db.close();
        } catch (SQLException exception) { }
    }

    private Connection getDb() {
        if (this.db != null) {
            try {
                if (!this.db.isClosed())
                    return this.db;
            } catch (SQLException exception) {
                this.getLogger().warning("SQLException while invoking isClosed()!");
                exception.printStackTrace();
            }

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
            this.insertTransaction = this.db.prepareStatement("INSERT INTO transactions (source, target, amount) VALUES (?, ?, ?)");
            this.selectBalance = this.db.prepareStatement("SELECT amount FROM accounts WHERE player=?");
            this.insertBalance = this.db.prepareStatement("INSERT INTO accounts (player, amount) VALUES (?, ?)");
            this.updateBalance = this.db.prepareStatement("UPDATE accounts SET amount=? WHERE player=?");

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

    private void logTransaction(UUID source, UUID target, int amount) throws SQLException {
        this.getDb();

        this.insertTransaction.setString(0, source.toString());
        this.insertTransaction.setString(1, target.toString());
        this.insertTransaction.setInt(2, amount);

        if (this.insertTransaction.executeUpdate() != 1)
            throw new SQLException("Unexpected number of affected rows!");

        this.insertTransaction.clearParameters();
    }

    public int getBalance(UUID player) throws SQLException {
        this.getDb();

        this.selectBalance.setString(0, player.toString());

        ResultSet result = this.selectBalance.executeQuery();

        this.selectBalance.clearParameters();

        if (!result.next())
            return 0;

        return result.getInt(0);
    }

    public void deposit(UUID player, int amount) throws SQLException, InsufficientFundsException {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero!");

        PlayerInventory inventory = this.getServer().getPlayer(player).getInventory();

        // How much gold is in the inventory?
        // Is it enough?
        int has = 0;
        for (ItemStack stack : inventory.all(this.currency).values()) {
            has += stack.getAmount();
        }

        if (has < amount)
            throw new InsufficientFundsException();

        if (this.getDb() == null)
            throw new SQLException("Database object is null!");

        boolean success = false;

        try {
            // Start transaction.
            this.db.setAutoCommit(false);

            // Log it.
            this.logTransaction(player, player, amount);

            // Remove from inventory.
            Map<Integer, ItemStack> failed = inventory.removeItem(new ItemStack(this.currency, amount));
            if (!failed.isEmpty()) {
                // Todo: This should never happen, but if it does the log will be incorrect. Figure it out...

                this.getLogger().warning("Failed to remove all items from inventory during deposit! Decreasing amount...");

                for (ItemStack stack : failed.values()) {
                    amount -= stack.getAmount();
                }
            }

            // Add to account.
            this.selectBalance.setString(0, player.toString());

            ResultSet result = this.selectBalance.executeQuery();

            this.selectBalance.clearParameters();

            if (result.next()) {
                this.updateBalance.setString(0, player.toString());
                this.updateBalance.setInt(1, this.getBalance(player) + amount);

                if (this.updateBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                this.updateBalance.clearParameters();
            } else {
                this.insertBalance.setString(0, player.toString());
                this.insertBalance.setInt(1, amount);

                if (this.insertBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                this.insertBalance.clearParameters();
            }

            result.close();

            // If we've gotten this far, commit the changes.
            this.db.commit();

            // Mark as success.
            success = true;
        } catch (SQLException exception) {
            this.getLogger().warning("SQLException while depositing currency!");
            exception.printStackTrace();

            try {
                if (this.db != null)
                    this.db.rollback();
            } catch (SQLException exception2) {
                this.getLogger().warning("SQLException while rolling back transaction!");
                exception2.printStackTrace();
            }
        } finally {
            this.db.setAutoCommit(true);
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }

    public void withdraw(UUID player, int amount) throws SQLException, InsufficientFundsException, CrampedInventoryException {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero!");

        // How much gold is in the account?
        // Is it enough?
        if (this.getBalance(player) < amount)
            throw new InsufficientFundsException();

        // Will it fit in the inventory?
        PlayerInventory inventory = this.getServer().getPlayer(player).getInventory();

        int canAdd = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType() == Material.AIR) { // Do we really need to check for air?
                canAdd += this.currency.getMaxStackSize();
            } else if (stack.getType() == this.currency) {
                canAdd += this.currency.getMaxStackSize() - stack.getAmount();
            }
        }

        if (canAdd < amount)
            throw new CrampedInventoryException();

        boolean success = false;

        if (this.getDb() == null)
            throw new SQLException("Database object is null!");

        try {
            this.db.setAutoCommit(false);

            this.logTransaction(player, player, - amount);

            this.updateBalance.setString(0, player.toString());
            this.updateBalance.setInt(1, this.getBalance(player) - amount);

            if (this.updateBalance.executeUpdate() != 1)
                throw new SQLException("Unexpected number of affected rows!");

            this.updateBalance.clearParameters();

            // Add to inventory.
            inventory.addItem(new ItemStack(this.currency, amount));

            // Looks good. Let's commit!
            this.db.commit();

            // Mark as success.
            success = true;
        } catch (SQLException exception) {
            this.getLogger().warning("SQLException while withdrawing currency!");
            exception.printStackTrace();

            try {
                if (this.db != null)
                    this.db.rollback();
            } catch (SQLException exception2) {
                this.getLogger().warning("SQLException while rolling back transaction!");
                exception2.printStackTrace();
            }
        } finally {
            this.db.setAutoCommit(true);
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }

    public void transfer(UUID source, UUID target, int amount) throws SQLException, InsufficientFundsException {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero!");

        // How much gold is in the account?
        // Is it enough?
        if (this.getBalance(source) < amount)
            throw new InsufficientFundsException();

        if (this.getDb() == null)
            throw new SQLException("Database object is null!");

        boolean success = false;

        try {
            this.db.setAutoCommit(false);

            this.logTransaction(source, target, amount);

            this.updateBalance.setString(0, source.toString());
            this.updateBalance.setInt(1, this.getBalance(source) - amount);

            if (this.updateBalance.executeUpdate() != 1)
                throw new SQLException("Unexpected number of affected rows!");

            this.updateBalance.clearParameters();

            this.selectBalance.setString(0, target.toString());

            ResultSet result = this.selectBalance.executeQuery();

            if (result.next()) {
                this.updateBalance.setString(0, target.toString());
                this.updateBalance.setInt(1, this.getBalance(target) + amount);

                if (this.updateBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                this.updateBalance.clearParameters();
            } else {
                this.insertBalance.setString(0, target.toString());
                this.insertBalance.setInt(1, amount);

                if (this.insertBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                this.insertBalance.clearParameters();
            }

            this.db.commit();

            success = true;
        } catch (SQLException exception) {
            this.getLogger().warning("SQLException while transferring currency!");
            exception.printStackTrace();

            try {
                if (this.db != null)
                    this.db.rollback();
            } catch (SQLException exception2) {
                this.getLogger().warning("SQLException while rolling back transaction!");
                exception2.printStackTrace();
            }
        } finally {
            this.db.setAutoCommit(true);
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }
}
