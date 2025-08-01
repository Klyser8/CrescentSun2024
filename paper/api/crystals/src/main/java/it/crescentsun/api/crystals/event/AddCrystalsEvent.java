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
 * Event called when one or more crystals are added to a player's vault.
 */
public class AddCrystalsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private int amount;
    private final UUID vaultUuid;
    private final Player owner;
    private final Location vaultLocation;
    private final CrystalSource source;
    private final boolean isVaultPublic;
    private boolean cancelled;
    public AddCrystalsEvent(int amount, UUID vaultUuid, Player owner, Location vaultLocation, @Nullable CrystalSource source, boolean isVaultPublic) {
        this.amount = amount;
        this.vaultUuid = vaultUuid;
        this.owner = owner;
        this.vaultLocation = vaultLocation;
        this.isVaultPublic = isVaultPublic;
        this.source = source;
    }

    /**
     * @return The number of crystals that are being added to the vault.
     */
    public int getAddedAmount() {
        return amount;
    }

    /**
     * Changes the number of crystals that are being spawned.
     *
     * @param amount The new number of crystals being added, must be greater than 0.
     */
    public void setAddedAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.amount = amount;
    }

    /**
     * @return The UUID of the vault where the crystals are being added.
     */
    public UUID getVaultUuid() {
        return vaultUuid;
    }

    /**
     * @return The player who owns the vault where the crystals are being added.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @return The location of the vault where the crystals are being added.
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
     * @return The reason the crystals are being added to the vault. 99% of the time this will be null, as the source is dictated upon spawning the crystals instead.
     */
    @Nullable public CrystalSource getSource() {
        return source;
    }
}
