package it.crescentsun.api.crystals.event;

import it.crescentsun.api.crystals.CrystalSource;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when one or more crystals are added to a player's vault.
 */
public class AddCrystalsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private int amount;
    private final CrystalSource source;
    private boolean cancelled;
    public AddCrystalsEvent(int amount, CrystalSource generationSource) {
        this.amount = amount;
        this.source = generationSource;
    }

    /**
     * @return The amount of crystals that are being spawned.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Changes the amount of crystals that are being spawned.
     *
     * @param amount The new amount of crystals, must be greater than 0.
     */
    public void setAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.amount = amount;
    }

    /**
     * @return The reason the crystals are being incremented.
     */
    public CrystalSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
