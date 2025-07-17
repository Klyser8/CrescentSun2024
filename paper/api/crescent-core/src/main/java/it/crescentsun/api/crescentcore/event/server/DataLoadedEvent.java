package it.crescentsun.api.crescentcore.event.server;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called after {@link ServerLoadEvent}, after initial database set up + data loading is complete.
 * <b>IMPORTANT</b>: This event is fired <u>before</u> any connection to the proxy is made!
 * To run logic after the connection to the proxy is made, use {@link ProxyConnectedEvent}.
 */
@SuppressWarnings("ALL")
public class DataLoadedEvent extends ServerLoadEvent {
    private static final HandlerList handlers = new HandlerList();

    /**
     * Creates a {@code ServerLoadEvent} with a given loading type.
     *
     * @param type the context in which the server was loaded
     */
    public DataLoadedEvent(@NotNull ServerLoadEvent.LoadType type) {
        super(type);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
