package it.crescentsun.api.artifacts.event;

import it.crescentsun.api.artifacts.item.Artifact;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player interacts with an artifact.
 * This may be when: <br>
 * - The player right clicks while holding the artifact. <br>
 * - The player left clicks while holding the artifact. <br>
 * - The player shift right clicks while holding the artifact. <br>
 * - The player shift left clicks while holding the artifact. <br>
 */
public class ArtifactInteractEvent extends ArtifactEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Action action;
    private final Player who;
    // Hand
    private final EquipmentSlot hand;
    private boolean cancelled;

    public ArtifactInteractEvent(Artifact artifact,
                                 ItemStack artifactStack,
                                 @NotNull Player who,
                                 Action action,
                                 EquipmentSlot hand) {
        super(artifact, artifactStack);
        this.action = action;
        if (hand != null && hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            throw new IllegalArgumentException("Invalid hand: " + hand);
        }
        this.hand = hand;
        this.who = who;
    }

    /**
     * @return The type of interaction that occurred.
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return The hand the player used to interact with the artifact.
     */
    @Nullable public EquipmentSlot getHand() {
        return hand;
    }

    /**
     * @return The player who interacted with the artifact.
     */
    public Player getPlayer() {
        return who;
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
