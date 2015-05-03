package it.flaten.playerlimit;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class PlayerLimitCommand extends Command {
    private final PlayerLimit playerLimit;

    public PlayerLimitCommand(PlayerLimit playerLimit) {
        super("playerlimit", "playerlimit.use");

        this.playerLimit = playerLimit;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
