package it.crescentsun.crescentcore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AutoSaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public AutoSaveEvent() {
        super();
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
