package it.crescentsun.crescentcore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the Player data entry registry is frozen, disallowing any more data entries to be registered.
 * Ideal if you need to run any logic after all player data entries have been registered.
 */
public class PlayerDataRegistryFreezeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
