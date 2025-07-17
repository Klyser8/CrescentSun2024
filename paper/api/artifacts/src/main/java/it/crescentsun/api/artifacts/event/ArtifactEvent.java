package it.crescentsun.api.artifacts.event;

import it.crescentsun.api.artifacts.item.Artifact;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an event which an artifact is involved in occurs.
 */
public abstract class ArtifactEvent extends Event {
    private final Artifact artifact;
    private final ItemStack artifactStack;

    protected ArtifactEvent(Artifact artifact, ItemStack artifactStack) {
        this.artifact = artifact;
        this.artifactStack = artifactStack;
    }

    /**
     * @return The artifact involved in the event.
     */
    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * @return The item stack of the artifact involved in the event.
     */
    public ItemStack getArtifactStack() {
        return artifactStack;
    }
}
