package it.crescentsun.crescentcore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the Plugin data registry is frozen, not allowing any more data to be registered.
 * Ideal if you need to run any logic after all plugin data has been registered.
 */
public class PluginDataRegistryFreezeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
