package it.flaten.bans;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.UUID;

public class BanCommand extends Command {
    private final Bans bans;

    public BanCommand(Bans bans) {
        super("ban", "bans.ban");

        this.bans = bans;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            TextComponent error = new TextComponent("Usage: /ban [player] [reason]");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        ProxiedPlayer player = this.bans.getProxy().getPlayer(args[0]);

        UUID uuid;
        if (player == null) {
            uuid = this.bans.getUuid(args[0]);

            if (uuid == null) {
                TextComponent error = new TextComponent("Could not find player.");
                error.setColor(ChatColor.RED);
                sender.sendMessage(error);
                return;
            }
        } else {
            uuid = player.getUniqueId();
        }

        String reason = null;
        if (args.length > 1) {
            String[] restArgs = new String[args.length - 1];
            System.arraycopy(args, 1, restArgs, 0, args.length - 1);
            reason = Joiner.on(' ').join(restArgs);
        }

        try {
            this.bans.ban(uuid, (sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null), reason);

            if (player != null) {
                this.bans.kick(player, "You were banned" + (reason != null && reason.length() > 0 ? ": " + reason : "."));
            }
        } catch (IllegalArgumentException exception) {
            TextComponent error = new TextComponent(exception.getMessage());
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        } catch (SQLException exception) {
            exception.printStackTrace();

            TextComponent error = new TextComponent("Internal error!");
            error.setColor(ChatColor.RED);
            sender.sendMessage(error);
            return;
        }

        TextComponent done = new TextComponent("You banned \"" + args[0] + "\"" + (reason != null && reason.length() > 0 ? ": " + reason : "."));
        done.setColor(ChatColor.GREEN);
        sender.sendMessage(done);
    }
}
