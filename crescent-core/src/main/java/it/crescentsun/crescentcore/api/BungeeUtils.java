package it.crescentsun.crescentcore.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

@SuppressWarnings("UnusedReturnValue")
public class BungeeUtils  {

    /**
     * Sends the specified player to the target server.
     *
     * @param sendingPlugin The JavaPlugin instance responsible for sending the player.
     * @param player        The Player to be sent to the target server.
     * @param server        The target server's name.
     * @return A CompletableFuture<Boolean> that completes with true if the player's data is
     * saved and the player is sent to the target server without any exceptions; false otherwise.
     */
    public static boolean sendPlayerToServer(JavaPlugin sendingPlugin, Player player, String server) {
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
    }

}
