package it.flaten.work;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class WorkCommand implements CommandExecutor {
    private final Work work;

    public WorkCommand(Work work) {
        this.work = work;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;

        try {
            this.work.toggleWork(player);

            player.sendMessage(ChatColor.GREEN + "Work inventory is now " + (this.work.isWork(player) ? "en" : "dis") + "abled.");
        } catch (IOException exception) {
            player.sendMessage(ChatColor.RED + "An error occurred while switching inventories.");

            this.work.getLogger().severe("IOException while toggling work inventory for player \"" + player.getName() + "\" (" + player.getUniqueId() + ")");

            exception.printStackTrace();
        }

        return true;
    }
}
