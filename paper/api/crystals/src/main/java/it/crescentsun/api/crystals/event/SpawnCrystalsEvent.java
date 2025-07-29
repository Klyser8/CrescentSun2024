package it.crescentsun.api.crystals.event;

import it.crescentsun.api.crystals.CrystalSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event called when one or more crystals are spawned anywhere in the network
 */
public class SpawnCrystalsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private int amount;
    private final CrystalSource source;
    private boolean cancelled;
    private Player owner;
    private Location location;
    public SpawnCrystalsEvent(@Nullable Player player, int amount, Location location, CrystalSource generationSource) {
        this.amount = amount;
        this.source = generationSource;
        this.owner = player;
        this.location = location;
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

    /**
     * @return The player for whom the crystals are being spawned, or null if the crystals are not being spawned for a specific player.
     */
    @Nullable public Player getOwner() {
        return owner;
    }

    /**
     * Sets the player for whom the crystals are being spawned.
     *
     * @param owner The player for whom the crystals are being spawned, or null if the crystals are not being spawned for a specific player.
     */
    public void setOwner(@Nullable Player owner) {
        this.owner = owner;
    }

    /**
     * @return The location where the crystals are being spawned.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location where the crystals are being spawned.
     *
     * @param location The new location where the crystals are being spawned.
     */
    public void setLocation(Location location) {
        this.location = location;
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
