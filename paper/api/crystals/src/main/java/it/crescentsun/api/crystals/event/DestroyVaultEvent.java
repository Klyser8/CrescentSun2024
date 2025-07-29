package it.crescentsun.api.crystals.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event called when a Vault is destroyed.
 */
public class DestroyVaultEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final UUID vaultUuid;
    private final Player owner;
    private final Location vaultLocation;
    private final boolean isVaultPublic;
    private boolean shouldDropCrystal;
    private boolean cancelled;
    public DestroyVaultEvent(UUID vaultUuid, Player owner, Location vaultLocation, boolean isVaultPublic, boolean shouldDropCrystal) {
        this.vaultUuid = vaultUuid;
        this.owner = owner;
        this.vaultLocation = vaultLocation;
        this.isVaultPublic = isVaultPublic;
        this.shouldDropCrystal = shouldDropCrystal;
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
     * @return true if the crystal used to create the vault should be dropped when the vault is destroyed, false otherwise.
     */
    public boolean shouldDropCrystal() {
        return shouldDropCrystal;
    }

    /**
     * Sets whether the crystal used to create the vault should be dropped when the vault is destroyed.
     * @param shouldDropCrystal true if the crystal should be dropped, false otherwise.
     */
    public void setShouldDropCrystal(boolean shouldDropCrystal) {
        this.shouldDropCrystal = shouldDropCrystal;
    }
}
