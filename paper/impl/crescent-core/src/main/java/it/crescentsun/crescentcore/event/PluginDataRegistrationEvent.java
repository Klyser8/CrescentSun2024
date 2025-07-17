package it.crescentsun.crescentcore.event;

import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when CrescentCore is ready to register plugin data.
 * This is the ideal time to register any plugin data that is required for your plugin.
 */
public class PluginDataRegistrationEvent extends Event {

    private final PluginDataRegistryService pluginDataRegistryService;

    private static final HandlerList handlers = new HandlerList();

    public PluginDataRegistrationEvent(PluginDataRegistryService pluginDataRegistryService) {
        this.pluginDataRegistryService = pluginDataRegistryService;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PluginDataRegistryService getRegistry() {
        return pluginDataRegistryService;
    }


}
