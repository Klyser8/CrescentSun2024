package it.klynet.klynetcore.api.event.player;

import it.klynet.klynetcore.core.data.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player's data is saved to the database.
 */
public class PlayerQuitEventPostDBSave extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final PlayerData playerData;

    public PlayerQuitEventPostDBSave(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        Bukkit.getLogger().info("PlayerDataSavedPostQuitEvent called for " + player.getName());
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
