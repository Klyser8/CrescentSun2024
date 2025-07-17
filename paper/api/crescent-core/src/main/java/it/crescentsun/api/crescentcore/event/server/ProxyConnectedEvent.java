package it.crescentsun.api.crescentcore.event.server;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;

/**
 * Event called after {@link DataLoadedEvent} and shortly after a connection with the proxy is established.
 * The calling of this event is slightly delayed, to allow for {@link it.crescentsun.crescentcore.CrescentCore#serverName}
 * and {@link it.crescentsun.crescentcore.CrescentCore#serverList} to be populated.
 */
@SuppressWarnings("ALL")
public class ProxyConnectedEvent extends ServerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
