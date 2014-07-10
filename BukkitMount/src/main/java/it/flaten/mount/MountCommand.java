package it.flaten.mount;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MountCommand implements CommandExecutor {
    private Mount mount;

    public MountCommand(Mount mount) {
        this.mount = mount;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
