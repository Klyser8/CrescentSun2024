package it.crescentsun.api.artifacts.event;

import it.crescentsun.api.artifacts.item.Artifact;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event that wraps the original InventoryClickEvent.
 */
public class ArtifactInventoryEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final InventoryClickEvent originalEvent;
    private final Artifact clickedArtifact;
    private final Artifact cursorArtifact;

    public ArtifactInventoryEvent(@NotNull InventoryClickEvent originalEvent, Artifact clickedArtifact, Artifact cursorArtifact) {
        super(originalEvent.isAsynchronous());
        this.originalEvent = originalEvent;
        this.clickedArtifact = clickedArtifact;
        this.cursorArtifact = cursorArtifact;
    }

    public InventoryClickEvent getOriginalEvent() {
        return originalEvent;
    }

    public Artifact getClickedArtifact() {
        return clickedArtifact;
    }

    public Artifact getCursorArtifact() {
        return cursorArtifact;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
