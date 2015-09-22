package it.flaten.bank;

import it.flaten.mysql.BukkitMySql;
import it.flaten.mysql.MySqlPool;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class Bank extends JavaPlugin {
    private static final String INSERT_TRANSACTION = "INSERT INTO transactions (source, target, amount) VALUES (?, ?, ?)";
    private static final String SELECT_BALANCE = "SELECT amount FROM accounts WHERE player=?";
    private static final String INSERT_BALANCE = "INSERT INTO accounts (player, amount) VALUES (?, ?)";
    private static final String UPDATE_BALANCE = "UPDATE accounts SET amount=? WHERE player=?";

    private BukkitMySql mySql;
    private MySqlPool mySqlPool;

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

        this.mySql = (BukkitMySql) this.getServer().getPluginManager().getPlugin("mysql");
        this.mySqlPool = this.mySql.getPool(this);

        try {
            this.mySqlPool.setUrl(this.getConfig().getString("mysql.url", "jdbc:mysql://localhost:3306/bank"));
            this.mySqlPool.setUser(this.getConfig().getString("mysql.username", "root"));
            this.mySqlPool.setPassword(this.getConfig().getString("mysql.password", ""));

            if (this.mySqlPool.getConnection() == null)
                throw new SQLException("Unable to get server connection from pool!");
        } catch (PropertyVetoException | SQLException exception) {
            exception.printStackTrace();

            this.getLogger().warning("Database connection test failed! Disabling plugin...");
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

        this.mySqlPool.close();
    }

    /**
     * Logs a currency transaction to the database.
     *
     * If source and target accounts are the same UUID, the entry represents either a deposit or withdrawal, depending
     * on the amount logged. A positive value means deposit (increases the account balance), while a negative value
     * means withdrawal (decreases the account balance).
     *
     * @param source UUID of source account.
     * @param target UUID of target account.
     * @param amount Amount of currency transferred.
     * @throws SQLException if the database backend throws one, or if the database has inconsistencies.
     */
    private void logTransaction(UUID source, UUID target, int amount) throws SQLException {
        try (
            Connection connection = this.mySqlPool.getConnection();
            PreparedStatement insertTransaction = connection.prepareStatement(INSERT_TRANSACTION)
        ) {
            insertTransaction.setString(1, source.toString());
            insertTransaction.setString(2, target.toString());
            insertTransaction.setInt(3, amount);

            if (insertTransaction.executeUpdate() != 1)
                throw new SQLException("Unexpected number of affected rows!");
        }
    }

    /**
     * Get the current balance of an account.
     *
     * Treats non-existent accounts as empty.
     *
     * @param player UUID of account to check.
     * @return Current account balance.
     * @throws SQLException if the database backend throws one.
     */
    public int getBalance(UUID player) throws SQLException {
        try (
            Connection connection = this.mySqlPool.getConnection();
            PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE)
        ) {
            selectBalance.setString(1, player.toString());

            try (ResultSet result = selectBalance.executeQuery()) {
                if (!result.next())
                    return 0;

                return result.getInt(1);
            }
        }
    }

    /**
     * Deposit currency from inventory to account.
     *
     * Moves currency from the given player's inventory to the account with matching UUID.
     *
     * @param player UUID of player and account to use.
     * @param amount Amount of currency to deposit. Must be greater than 0.
     * @throws SQLException if the database is unavailable, throws an exception, or has inconsistencies.
     * @throws InsufficientFundsException if the player does not have enough currency in the inventory.
     */
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

        boolean success = false;

        try (Connection connection = this.mySqlPool.getConnection()) {
            try (
                PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE);
                PreparedStatement insertBalance = connection.prepareStatement(INSERT_BALANCE);
                PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE)
            ) {
                // Start transaction.
                connection.setAutoCommit(false);

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
                selectBalance.setString(1, player.toString());

                try (ResultSet result = selectBalance.executeQuery()) {
                    if (result.next()) {
                        updateBalance.setInt(1, this.getBalance(player) + amount);
                        updateBalance.setString(2, player.toString());

                        if (updateBalance.executeUpdate() != 1)
                            throw new SQLException("Unexpected number of affected rows!");
                    } else {
                        insertBalance.setString(1, player.toString());
                        insertBalance.setInt(2, amount);

                        if (insertBalance.executeUpdate() != 1)
                            throw new SQLException("Unexpected number of affected rows!");
                    }

                    // If we've gotten this far, commit the changes.
                    connection.commit();

                    // Mark as success.
                    success = true;
                }
            } catch (SQLException exception) {
                this.getLogger().warning("SQLException while depositing currency!");
                exception.printStackTrace();

                try {
                    connection.rollback();
                } catch (SQLException exception2) {
                    this.getLogger().warning("SQLException while rolling back transaction!");
                    exception2.printStackTrace();
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }

    /**
     * Withdraw currency from an account to inventory.
     *
     * Moves currency from the given account to the player with matching UUID's account.
     *
     * @param player UUID of player and account to use.
     * @param amount Amount of currency to withdraw. Must be greater than 0.
     * @throws SQLException if the database is unavailable, throws an exception, or has inconsistencies.
     * @throws InsufficientFundsException if the player does not have enough currency in the account.
     * @throws CrampedInventoryException if the player does not have enough room in the inventory.
     */
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

        try (Connection connection = this.mySqlPool.getConnection()) {
            try (PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE)) {
                connection.setAutoCommit(false);

                this.logTransaction(player, player, -amount);

                updateBalance.setInt(1, this.getBalance(player) - amount);
                updateBalance.setString(2, player.toString());

                if (updateBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                // Add to inventory.
                inventory.addItem(new ItemStack(this.currency, amount));

                // Looks good. Let's commit!
                connection.commit();

                // Mark as success.
                success = true;
            } catch (SQLException exception) {
                this.getLogger().warning("SQLException while withdrawing currency!");
                exception.printStackTrace();

                try {
                    connection.rollback();
                } catch (SQLException exception2) {
                    this.getLogger().warning("SQLException while rolling back transaction!");
                    exception2.printStackTrace();
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }

    /**
     * Transfer currency between two accounts.
     *
     * Moves currency from the given source account to the given target account.
     *
     * @param source UUID of source account.
     * @param target UUID of target account.
     * @param amount Amount of currency to transfer.
     * @throws SQLException if the database is unavailable, throws an exception, or has inconsistencies.
     * @throws InsufficientFundsException if there is too little currency in the source account.
     */
    public void transfer(UUID source, UUID target, int amount) throws SQLException, InsufficientFundsException {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero!");

        if (source.equals(target))
            throw new IllegalArgumentException("Target account must differ from source account!");

        // How much gold is in the account?
        // Is it enough?
        if (this.getBalance(source) < amount)
            throw new InsufficientFundsException();

        boolean success = false;

        try (Connection connection = this.mySqlPool.getConnection()) {
            try (
                PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE);
                PreparedStatement insertBalance = connection.prepareStatement(INSERT_BALANCE);
                PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE)
            ) {
                connection.setAutoCommit(false);

                this.logTransaction(source, target, amount);

                updateBalance.setInt(1, this.getBalance(source) - amount);
                updateBalance.setString(2, source.toString());

                if (updateBalance.executeUpdate() != 1)
                    throw new SQLException("Unexpected number of affected rows!");

                updateBalance.clearParameters();

                selectBalance.setString(1, target.toString());

                try (ResultSet result = selectBalance.executeQuery()) {
                    if (result.next()) {
                        updateBalance.setInt(1, this.getBalance(target) + amount);
                        updateBalance.setString(2, target.toString());

                        if (updateBalance.executeUpdate() != 1)
                            throw new SQLException("Unexpected number of affected rows!");
                    } else {
                        insertBalance.setString(1, target.toString());
                        insertBalance.setInt(2, amount);

                        if (insertBalance.executeUpdate() != 1)
                            throw new SQLException("Unexpected number of affected rows!");
                    }

                    connection.commit();

                    success = true;
                }
            } catch (SQLException exception) {
                this.getLogger().warning("SQLException while transferring currency!");
                exception.printStackTrace();

                try {
                    connection.rollback();
                } catch (SQLException exception2) {
                    this.getLogger().warning("SQLException while rolling back transaction!");
                    exception2.printStackTrace();
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }

        if (!success)
            throw new SQLException("Unknown error!");
    }
}
