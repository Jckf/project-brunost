package it.flaten.chat;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChatCommand extends Command {
    private final Chat chat;

    private final String command;

    public ChatCommand(Chat chat, String command) {
        super(command, "chat.command." + command);

        this.chat = chat;
        this.command = command;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            TextComponent error = new TextComponent("This command can only be used in-game.");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);

            return;
        }

        if (args.length == 0) {
            TextComponent error = new TextComponent("You need to enter a message to send.");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);

            return;
        }

        this.chat.commandBroadcast(this.command, (ProxiedPlayer) sender, Joiner.on(" ").join(args));
    }
}
