package it.flaten.bank;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankCommand implements TabExecutor {
    private Bank bank;

    public BankCommand(Bank bank) {
        this.bank = bank;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
            return false;

        String subCommand = args[0];

        String[] subArgs = {};
        if (args.length > 1)
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        switch (subCommand) {
            case "balance": return this.balance(player, subArgs);
            case "deposit": return this.deposit(player, subArgs);
            case "withdraw": return this.withdraw(player, subArgs);
            case "transfer": return this.transfer(player, subArgs);
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length != 1)
            return completions;

        if ("balance".startsWith(args[0]))
            completions.add("balance");

        if ("deposit".startsWith(args[0]))
            completions.add("deposit");

        if ("withdraw".startsWith(args[0]))
            completions.add("withdraw");

        if ("transfer".startsWith(args[0]))
            completions.add("transfer");

        return completions;
    }

    private boolean balance(Player player, String[] args) {
        try {
            player.sendMessage(ChatColor.GREEN + "Your balance is: " + this.bank.getBalance(player.getUniqueId()));
        } catch (SQLException exception) {
            player.sendMessage(ChatColor.RED + "Internal error.");
        }

        return true;
    }

    private boolean deposit(Player player, String[] args) {
        if (args.length != 1)
            return false;

        int amount = 0;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            return false;
        }

        try {
            this.bank.deposit(player.getUniqueId(), amount);
        } catch (InsufficientFundsException exception) {
            player.sendMessage(ChatColor.RED + "Insufficient funds.");
            return true;
        } catch (SQLException exception) {
            player.sendMessage(ChatColor.RED + "Internal error.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Transaction completed.");
        return true;
    }

    private boolean withdraw(Player player, String[] args) {
        if (args.length != 1)
            return false;

        int amount = 0;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            return false;
        }

        try {
            this.bank.withdraw(player.getUniqueId(), amount);
        } catch (InsufficientFundsException exception) {
            player.sendMessage(ChatColor.RED + "Insufficient funds.");
            return true;
        } catch (CrampedInventoryException exception) {
            player.sendMessage(ChatColor.RED + "There is not enough room in your inventory.");
            return true;
        } catch (SQLException exception) {
            player.sendMessage(ChatColor.RED + "Internal error.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Transaction completed.");
        return true;
    }

    private boolean transfer(Player player, String[] args) {
        if (args.length != 2)
            return false;

        int amount = 0;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            return false;
        }

        OfflinePlayer target = this.bank.getServer().getOfflinePlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Unknown recipient.");
            return true;
        }

        try {
            this.bank.transfer(player.getUniqueId(), target.getUniqueId(), amount);
        } catch (InsufficientFundsException exception) {
            player.sendMessage(ChatColor.RED + "Insufficient funds.");
            return true;
        } catch (SQLException exception) {
            player.sendMessage(ChatColor.RED + "Internal error.");
            return true;
        }

        if (target.isOnline())
            target.getPlayer().sendMessage(ChatColor.GREEN + "Funds were added to your bank account.");

        player.sendMessage(ChatColor.GREEN + "Transaction complete.");
        return true;
    }
}
