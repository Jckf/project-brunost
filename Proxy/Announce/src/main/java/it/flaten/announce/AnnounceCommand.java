package it.flaten.announce;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class AnnounceCommand extends Command {
    private final Announce announce;

    public AnnounceCommand(Announce announce) {
        super("announce", "announce.announce");

        this.announce = announce;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0)
            args = new String[]{ "" };

        boolean okay = false;
        switch (args[0]) {
            case "list":   okay = this.list(sender, args);   break;
            case "add":    okay = this.add(sender, args);    break;
            case "remove": okay = this.remove(sender, args); break;
        }

        if (!okay)
            this.help(sender, args);
    }

    private boolean help(CommandSender sender, String[] args) {
        TextComponent space = new TextComponent(" ");

        TextComponent usage = new TextComponent("Usage:");
        usage.setColor(ChatColor.RED);

        TextComponent list = new TextComponent("/announce list");
        list.setColor(ChatColor.RED);

        TextComponent add = new TextComponent("/announce add");
        add.setColor(ChatColor.RED);

        TextComponent addInitialDelay = new TextComponent("[initial delay]");
        addInitialDelay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("Number of seconds before first announcement is made.").create()
        ));

        TextComponent addInterval = new TextComponent("[interval]");
        addInterval.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("Number of seconds between each repetition of the announcement. Zero to not repeat.").create()
        ));

        TextComponent addMessage = new TextComponent("[message]");
        addMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("Message to announce. Use ampersands for formatting codes.").create()
        ));

        TextComponent remove = new TextComponent("/announce remove");
        remove.setColor(ChatColor.RED);

        TextComponent removeId = new TextComponent("[id]");
        removeId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("Id of the announcement to remove, as shown in \"/announce list\".").create()
        ));

        sender.sendMessage(usage);
        sender.sendMessage(list);
        sender.sendMessage(add, space, addInitialDelay, space, addInterval, space, addMessage);
        sender.sendMessage(remove, space, removeId);

        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        if (this.announce.getAnnouncements().isEmpty()) {
            TextComponent empty = new TextComponent("No active announcements.");
            empty.setColor(ChatColor.RED);

            sender.sendMessage(empty);

            return true;
        }

        TextComponent header = new TextComponent("Active announcements:");
        header.setColor(ChatColor.YELLOW);

        sender.sendMessage(header);

        for (ScheduledTask task : this.announce.getAnnouncements().values()) {
            AnnounceTask announceTask = (AnnounceTask) task.getTask();

            TextComponent id = new TextComponent(task.getId() + ": ");
            id.setColor(ChatColor.YELLOW);

            TextComponent message = new TextComponent(announceTask.getMessage());
            message.setColor(ChatColor.DARK_GREEN);

            sender.sendMessage(id, message);
        }

        return true;
    }

    private boolean add(CommandSender sender, String[] args) {
        if (args.length < 4)
            return false;

        int initialDelay = 0;
        int interval = 0;

        try {
            initialDelay = Integer.parseInt(args[1]);
            interval = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            return false;
        }

        String[] message = new String[args.length - 3];
        System.arraycopy(args, 3, message, 0, args.length - 3);

        this.announce.scheduleAnnouncement(
            this.announce.parseMessage(Joiner.on(" ").join(message)),
            initialDelay,
            interval
        );

        TextComponent success = new TextComponent("Announcement scheduled.");
        success.setColor(ChatColor.GREEN);

        sender.sendMessage(success);

        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        if (args.length != 2)
            return false;

        int task = 0;

        try {
            task = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            return false;
        }

        if (!this.announce.cancelAnnouncement(task)) {
            TextComponent error = new TextComponent("Could not cancel announcement.");
            error.setColor(ChatColor.RED);

            sender.sendMessage(error);
        } else {
            TextComponent success = new TextComponent("Announcement cancelled.");
            success.setColor(ChatColor.GREEN);

            sender.sendMessage(success);
        }

        return true;
    }
}
