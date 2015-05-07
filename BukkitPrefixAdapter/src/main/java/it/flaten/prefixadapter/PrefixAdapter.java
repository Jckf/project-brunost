package it.flaten.prefixadapter;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PrefixAdapter extends JavaPlugin {
    public Chat chat;

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
}
