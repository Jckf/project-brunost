package it.flaten.chat;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TellCommand extends NameCompleteCommand {
    private final Chat chat;

    public TellCommand(Chat chat) {
        super(chat, "tell", "chat.tell", "msg", "m");

        this.chat = chat;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            TextComponent error = new TextComponent("This command can only be used in-game.");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);

            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            TextComponent error = new TextComponent("Usage: /tell <player> <message>");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);

            return;
        }

        ProxiedPlayer target = this.chat.getProxy().getPlayer(args[0]);

        if (target == null) {
            TextComponent error = new TextComponent("Could not find player \"" + args[0] + "\".");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);

            return;
        }

        this.chat.setReplyTo(player, target);
        this.chat.setReplyTo(target, player);

        System.arraycopy(args, 1, args, 0, args.length - 1);
        args[args.length - 1] = null;

        BaseComponent message = this.chat.formatMessage(player, target, Joiner.on(" ").skipNulls().join(args));

        sender.sendMessage(message);
        target.sendMessage(message);
    }
}
