package it.crescentsun.artifacts.event;

import it.crescentsun.api.artifacts.ArtifactRegistryService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArtifactRegistrationEvent extends Event {

    private final ArtifactRegistryService registryService;
    private static final HandlerList handlers = new HandlerList();

    public ArtifactRegistrationEvent(ArtifactRegistryService registryService) {
        this.registryService = registryService;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ArtifactRegistryService getRegistryService() {
        return registryService;
    }

}
