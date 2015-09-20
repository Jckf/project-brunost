package it.flaten.bans;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HistoryCommand extends Command {
    private final Bans bans;

    public HistoryCommand(Bans bans) {
        super("history", "bans.history");

        this.bans = bans;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 && !(sender instanceof ProxiedPlayer) || args.length > 1) {
            TextComponent error = new TextComponent("Usage: /history [player]");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        ProxiedPlayer who = args.length > 0 ? this.bans.getProxy().getPlayer(args[0]) : (ProxiedPlayer) sender;

        if (who == null) {
            TextComponent error = new TextComponent("Could not find player.");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        TextComponent header = new TextComponent("History for \"" + who.getName() + "\":");
        header.setColor(ChatColor.GREEN);
        sender.sendMessage(header);

        for (String entry : this.bans.getHistory(who)) {
            sender.sendMessage(new TextComponent(entry));
        }
    }
}
