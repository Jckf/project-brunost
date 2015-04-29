package it.flaten.chat;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;

public abstract class NameCompleteCommand extends Command implements TabExecutor {
    private final Plugin plugin;

    public NameCompleteCommand(Plugin plugin, String command, String permission, String... aliases) {
        super(command, permission, aliases);

        this.plugin = plugin;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        ArrayList<String> results = new ArrayList<>();

        String searchFor = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        for (ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
            if (player.getName().toLowerCase().startsWith(searchFor))
                results.add(player.getName());
        }

        return results;
    }
}
