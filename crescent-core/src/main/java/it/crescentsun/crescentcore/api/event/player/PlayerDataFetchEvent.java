package it.crescentsun.crescentcore.api.event.player;

import it.crescentsun.crescentcore.core.data.player.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player's data is read from their {@link PlayerData} object.
 */
public class PlayerDataFetchEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerData playerData;
    private final NamespacedKey dataKey;
    private final Object value;
    public PlayerDataFetchEvent(PlayerData playerData, NamespacedKey dataKey, Object value, boolean async) {
        super(playerData.getPlayer(), async);
        this.playerData = playerData;
        this.dataKey = dataKey;
        this.value = value;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public NamespacedKey getDataKey() {
        return dataKey;
    }

    public Object getValue() {
        return value;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
