package it.crescentsun.api.crystals.event;

import it.crescentsun.api.crystals.CrystalSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Event called when one or more crystals are removed from  a player's vault.
 */
public class RemoveCrystalsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private int amount;
    private final UUID vaultUuid;
    private final Player owner;
    private final Location vaultLocation;
    private boolean cancelled;
    private final CrystalSource source;
    private final boolean isVaultPublic;
    public RemoveCrystalsEvent(int amount, UUID vaultUuid, Player owner, Location vaultLocation, @Nullable CrystalSource source, boolean isVaultPublic) {
        this.amount = amount;
        this.vaultUuid = vaultUuid;
        this.owner = owner;
        this.vaultLocation = vaultLocation;
        this.source = source;
        this.isVaultPublic = isVaultPublic;
    }

    /**
     * @return The number of crystals that are being removed from the vault.
     */
    public int getRemovedAmount() {
        return amount;
    }

    /**
     * Changes the number of crystals that are being removed from the owner's vault.
     *
     * @param amount The new number of crystals being removed, must be greater than 0.
     */
    public void setRemovedAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.amount = amount;
    }

    /**
     * @return The UUID of the vault where the crystals are being removed.
     */
    public UUID getVaultUuid() {
        return vaultUuid;
    }

    /**
     * @return The player who owns the vault where the crystals are being removed.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @return The location of the vault where the crystals are being removed.
     */
    public Location getVaultLocation() {
        return vaultLocation;
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

    public boolean isVaultPublic() {
        return isVaultPublic;
    }

    /**
     * @return The reason the crystals are being removed. 99% of the time this will be null, since the source is usually provided upon spawning the crystals.
     */
    @Nullable public CrystalSource getSource() {
        return source;
    }
}
