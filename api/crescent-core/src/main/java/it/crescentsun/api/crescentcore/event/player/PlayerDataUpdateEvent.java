package it.crescentsun.api.crescentcore.event.player;

import it.crescentsun.api.crescentcore.data.player.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when any {@link DataEntry} pertaining to a player is updated.
 * You can use this event to cancel the update, or change the value that is being set.
 * IMPORTANT: if you want to change the value, you must ensure that the new value is of the same type as the old value.
 */
public class PlayerDataUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final PlayerData playerData;
    private final NamespacedKey dataKey;
    private Object value;
    public PlayerDataUpdateEvent(PlayerData playerData, NamespacedKey dataKey, Object value, boolean async) {
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

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
