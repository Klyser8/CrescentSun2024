package it.crescentsun.crescentcore.api.event.server;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called after {@link ServerLoadPostDBSetupEvent} and shortly after a connection with the proxy is established.
 * The calling of this event is slightly delayed, to allow for {@link it.crescentsun.crescentcore.CrescentCore#serverName}
 * and {@link it.crescentsun.crescentcore.CrescentCore#serverList} to be populated.
 */
@SuppressWarnings("ALL")
public class ProtoweaverConnectionEstablishedEvent extends ServerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
