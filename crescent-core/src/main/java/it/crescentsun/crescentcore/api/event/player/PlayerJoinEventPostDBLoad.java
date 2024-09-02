package it.crescentsun.crescentcore.api.event.player;

import it.crescentsun.crescentcore.api.data.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player's data is loaded from the database, after joining the server.
 */
public class PlayerJoinEventPostDBLoad extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final PlayerData playerData;

    public PlayerJoinEventPostDBLoad(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
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
