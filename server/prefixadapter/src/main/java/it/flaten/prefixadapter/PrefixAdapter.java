package it.flaten.prefixadapter;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PrefixAdapter extends JavaPlugin {
    private Chat chat;

    @Override
    public void onEnable() {
        this.getLogger().info("Fetching chat provider...");

        RegisteredServiceProvider<Chat> rsp = this.getServer().getServicesManager().getRegistration(Chat.class);

        this.chat = rsp.getProvider();

        if (this.chat == null) {
            this.getLogger().warning("No chat provider found! Disabling...");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.getLogger().info("Binding to BungeeCord PMC...");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.getLogger().info("Registering event handlers...");

        this.getServer().getPluginManager().registerEvents(new PrefixAdapterListener(this), this);

        this.getLogger().info("Resetting main scoreboard...");

        for (Team team : this.getServer().getScoreboardManager().getMainScoreboard().getTeams()) {
            team.unregister();
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unregistering event handlers...");

        HandlerList.unregisterAll(this);

        this.getLogger().info("Unbinding from BungeeCord PMC...");

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public void sendPrefix(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Prefix");
        out.writeUTF(this.chat.getPlayerPrefix(player));

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void setPlayerListName(Player player) {
        player.setPlayerListName(this.chat.getPlayerPrefix(player) + player.getName());
    }

    public void setNameTag(Player player) {
        String group = this.chat.getPrimaryGroup(player);
        Scoreboard scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(group);
        if (team == null) {
            team = scoreboard.registerNewTeam(group);
            team.setPrefix(this.chat.getGroupPrefix(player.getWorld(), player.getName()));
        }

        team.addPlayer(player);
    }

    public Chat getChat() {
        return this.chat;
    }
}
