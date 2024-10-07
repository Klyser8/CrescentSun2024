package it.crescentsun.api.artifacts.event;

import it.crescentsun.api.artifacts.item.Artifact;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an artifact is dropped in the world.
 */
public class ArtifactDropEvent extends ArtifactEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Item itemEntity;
    private boolean cancelled;

    protected ArtifactDropEvent(Artifact artifact, ItemStack artifactStack, Item itemEntity) {
        super(artifact, artifactStack);
        this.itemEntity = itemEntity;
    }

    /**
     * @return The item entity of the dropped artifact.
     */
    public Item getItemEntity() {
        return itemEntity;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
