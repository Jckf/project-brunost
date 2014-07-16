package it.flaten.mount;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmountCommand implements CommandExecutor {
    private Mount mount;

    public UnmountCommand(Mount mount) {
        this.mount = mount;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1)
            return false;

        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Specify a player to use this command from console!");
            return true;
        }

        Player player = args.length == 0 ? (Player) sender : this.mount.getServer().getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find player \"" + args[0] + "\"!");
            return true;
        }

        try {
            this.mount.unmount(player.getUniqueId());
        } catch (PlayerOfflineException ignored) {
            // We should never end up here.
        } catch (NoVehicleException exception) {
            sender.sendMessage(ChatColor.RED + (args.length == 0 ? "You are" : player.getName() + " is") + " not riding anyone/anything!");
        }

        return true;
    }
}
