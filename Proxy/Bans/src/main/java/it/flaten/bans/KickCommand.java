package it.flaten.bans;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class KickCommand extends Command {
    private final Bans bans;

    public KickCommand(Bans bans) {
        super("kick", "bans.kick");

        this.bans = bans;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            TextComponent error = new TextComponent("Usage: /kick [player] [reason]");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        ProxiedPlayer player = this.bans.getProxy().getPlayer(args[0]);

        if (player == null) {
            TextComponent error = new TextComponent("Could not find player.");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        String reason = null;
        if (args.length > 1) {
            String[] restArgs = new String[args.length - 1];
            System.arraycopy(args, 1, restArgs, 0, args.length - 1);
            reason = Joiner.on(' ').join(restArgs);
        }

        this.bans.kick(player, "You were kicked" + (reason != null && reason.length() > 0 ? ": " + reason : "."));

        TextComponent done = new TextComponent("You kicked \"" + player.getName() + "\".");
        done.setColor(ChatColor.GREEN);
        sender.sendMessage(done);
    }
}
