package it.klynet.klynetcore.api;

import it.klynet.klynetcore.KlyNetCore;
import it.klynet.klynetcore.core.data.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeeUtils {

    /**
     * Saves the player's data to the database, removes the data from the plugin's hashmap,
     * and sends the player to the specified target server.
     *
     * @param klyNetCore       The KlyNetCore instance.
     * @param sendingPlugin    The JavaPlugin instance responsible for sending the player.
     * @param player           The Player to be sent to the target server.
     * @param server           The target server's name.
     * @return A CompletableFuture<Boolean> that completes with true if the player's data is
     *         saved and the player is sent to the target server without any exceptions; false otherwise.
     */
    public static CompletableFuture<Boolean> saveDataAndSendPlayerToServer(
            KlyNetCore klyNetCore, JavaPlugin sendingPlugin, Player player, String server) {
        UUID uuid = player.getUniqueId();
        CompletableFuture<PlayerData> playerDataFut = klyNetCore.getPlayerManager().asyncSaveData(uuid);
        return playerDataFut.thenApplyAsync(playerData -> {
            if (playerData == null) {
                return false;
            }
            klyNetCore.getPlayerManager().removeData(uuid);
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("Connect");
                out.writeUTF(server);
                player.sendPluginMessage(sendingPlugin, "BungeeCord", b.toByteArray());
                b.close();
                out.close();
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error when trying to connect to " + server);
                e.printStackTrace();
                return false;
            }
        });
    }

}
