package it.crescentsun.api.artifacts.event;

import com.google.common.collect.ImmutableList;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Called when an artifact's flags are changed.
 * The artifact which flags are changed may be an item entity. If it is, the item entity is provided.
 */
public class ArtifactFlagChangeEvent extends ArtifactEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ImmutableList<ArtifactFlag> addedFlags;
    private final ImmutableList<ArtifactFlag> removedFlags;
    @Nullable private final Item itemEntity;

    public ArtifactFlagChangeEvent(Artifact artifact, ItemStack artifactStack, @Nullable Item itemEntity, @Nullable List<ArtifactFlag> addedFlags, @Nullable List<ArtifactFlag> removedFlags) {
        super(artifact, artifactStack);
        this.itemEntity = itemEntity;
        if (addedFlags == null) {
            addedFlags = List.of();
        }
        if (removedFlags == null) {
            removedFlags = List.of();
        }
        if (addedFlags.isEmpty() && removedFlags.isEmpty()) {
            throw new IllegalArgumentException("Both added and removed flags are empty. Why are you calling this event?");
        }
        this.addedFlags = ImmutableList.copyOf(addedFlags);
        this.removedFlags = ImmutableList.copyOf(removedFlags);
    }

    /**
     * Gets the item entity that is about to have its flags changed.
     * This may be null as the artifact which flags are changed may be an ItemStack untied to an Item entity.
     * @return The item entity that is about to have its flags changed, null if the artifact is not an item entity.
     */
    public @Nullable Item getItemEntity() {
        return itemEntity;
    }

    /**
     * Gets the flags that are about to be added to the artifact.
     * @return The flags that were added to the artifact. Can be empty.
     */
    public ImmutableList<ArtifactFlag> getAddedFlags() {
        return addedFlags;
    }

    /**
     * Gets the flags that are about to be removed from the artifact.
     * @return The flags that were removed from the artifact. Can be empty.
     */
    public ImmutableList<ArtifactFlag> getRemovedFlags() {
        return removedFlags;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
