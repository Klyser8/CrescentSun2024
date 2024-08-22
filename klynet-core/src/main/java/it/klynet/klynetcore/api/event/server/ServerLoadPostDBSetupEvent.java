package it.klynet.klynetcore.api.event.server;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;

/**
 * Event called after the server has loaded and the database has been set up.
 */
public class ServerLoadPostDBSetupEvent extends ServerEvent {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
