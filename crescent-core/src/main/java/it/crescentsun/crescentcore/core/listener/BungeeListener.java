package it.crescentsun.crescentcore.core.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BungeeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("GetServer")) {
            String serverNameUTF = in.readUTF();
            CompletableFuture<String> futureServerName = CompletableFuture.completedFuture(serverNameUTF);
            futureServerName.thenAcceptAsync(serverName -> {
                System.out.println("Server name: " + serverName);
                // Notify the player of the server they are on
                player.sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_PLAYER.getFormattedMessage(player.locale(), serverName));
                // Notify other players of the player's join
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player)) {
                        p.sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_OTHER.getFormattedMessage(p.locale(), player.getName()));
                    }
                });
                // Notify the console of the player's join
                Bukkit.getConsoleSender().sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_OTHER.getFormattedMessage(null, player.getName()));
            });
        }
    }

}
