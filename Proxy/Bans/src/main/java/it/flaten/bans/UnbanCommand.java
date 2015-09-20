package it.flaten.bans;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.UUID;

public class UnbanCommand extends Command {
    private final Bans bans;

    public UnbanCommand(Bans bans) {
        super("unban", "bans.unban");

        this.bans = bans;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            TextComponent error = new TextComponent("Usage: /unban [player] [reason]");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        UUID player = this.bans.getUuid(args[0]);

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
        try {
            this.bans.unban(player, reason);
        } catch (SQLException exception) {
            exception.printStackTrace();

            TextComponent error = new TextComponent("Internal error!");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }
        TextComponent done = new TextComponent("You unbanned \"" + args[0] + "\"" + (reason != null && reason.length() > 0 ? ": " + reason : "."));
        done.setColor(ChatColor.GREEN);
        sender.sendMessage(done);
    }
}
