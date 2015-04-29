package it.flaten.chat;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ReplyCommand extends NameCompleteCommand {
    private final Chat chat;

    public ReplyCommand(Chat chat) {
        super(chat, "reply", "chat.tell", "r");

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

        ProxiedPlayer target = this.chat.getReplyTo(player);

        if (target == null) {
            TextComponent error = new TextComponent("Nobody to reply to.");
            error.setColor(ChatColor.RED);

            player.sendMessage(error);

            return;
        }

        // A little bit of a hack.
        this.chat.getProxy().getPluginManager().dispatchCommand(sender, "tell " + target.getName() + " " + Joiner.on(" ").join(args));
    }
}
