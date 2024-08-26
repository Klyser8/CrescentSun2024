package it.crescentsun.crescentcore.api.event.crystals;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event called when crystals are spawned for a player.
 * NOTE: This event is called by the Crystals plugin, and is written here to decouple the network plugins.
 */
public class GenerateCrystalsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private int amount;
    private final CrystalGenerationSource generationSource;
    private Player player;
    private boolean cancelled;
    public GenerateCrystalsEvent(int amount, CrystalGenerationSource generationSource, @Nullable Player player) {
        this.amount = amount;
        this.generationSource = generationSource;
        this.player = player;
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
     * @return who the crystals are being spawned for. Null if the crystals are being spawned for anyone.
     */
    @Nullable public Player getPlayer() {
        return player;
    }

    /**
     * Changes who the crystals are being spawned for.
     * Set to null if you'd like the crystals to be spawned for anyone.
     *
     * @param player The new player to spawn the crystals for, or null.
     */
    public void setPlayer(@Nullable Player player) {
        this.player = player;
    }

    /**
     * @return The reason the crystals are being spawned.
     */
    public CrystalGenerationSource getGenerationSource() {
        return generationSource;
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
