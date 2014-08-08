package it.flaten.work;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWorkCommand implements CommandExecutor {
    private final Work work;

    public SetWorkCommand(Work work) {
        this.work = work;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
            return true;
        }

        this.work.setWork(((Player) sender).getInventory());

        sender.sendMessage(ChatColor.GREEN + "Work inventory template updated.");

        return true;
    }
}
