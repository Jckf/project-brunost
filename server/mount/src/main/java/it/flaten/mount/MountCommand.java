package it.flaten.mount;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MountCommand implements CommandExecutor {
    private Mount mount;

    public MountCommand(Mount mount) {
        this.mount = mount;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args.length > 2)
            return false;

        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Specify two players to use this command from console!");
            return true;
        }

        Player top = args.length == 1 ? (Player) sender : this.mount.getServer().getPlayer(args[0]);

        if (top == null) {
            sender.sendMessage(ChatColor.RED + "Could not find player \"" + args[0] + "\"!");
            return true;
        }

        Player bottom = this.mount.getServer().getPlayer(args[args.length - 1]);

        if (bottom == null) {
            sender.sendMessage(ChatColor.RED + "Could not find player \"" + args[args.length - 1] + "\"!");
            return true;
        }

        try {
            this.mount.mount(top.getUniqueId(), bottom.getUniqueId());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "You cannot mount someone on themselves!");
        } catch (PlayerOfflineException ignored) {
            // We should never end up here.
        } catch (OccupiedException exception) {
            sender.sendMessage(ChatColor.RED + bottom.getName() + " already has a passenger!");
        }

        return true;
    }
}
