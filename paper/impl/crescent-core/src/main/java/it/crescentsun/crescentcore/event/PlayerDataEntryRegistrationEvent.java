package it.crescentsun.crescentcore.event;

import it.crescentsun.api.crescentcore.data.player.PlayerDataRegistryService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when CrescentCore is ready to register player data entries.
 * This is the ideal time to register any player data entries that are required for your plugin.
 */
public class PlayerDataEntryRegistrationEvent extends Event {

    private final PlayerDataRegistryService playerDataRegistryService;

    private static final HandlerList handlers = new HandlerList();

    public PlayerDataEntryRegistrationEvent(PlayerDataRegistryService playerDataRegistryService) {
        this.playerDataRegistryService = playerDataRegistryService;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerDataRegistryService getRegistry() {
        return playerDataRegistryService;
    }

}
