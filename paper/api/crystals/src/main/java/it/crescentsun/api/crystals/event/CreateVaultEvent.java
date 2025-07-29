package it.crescentsun.api.crystals.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event called when a player creates a new vault.
 * This event is fired before the vault is initialized and saved.
 */
public class CreateVaultEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final UUID vaultUuid;
    private final Player owner;
    private final Location vaultLocation;
    private final boolean isVaultPublic;
    private boolean cancelled;
    public CreateVaultEvent(UUID vaultUuid, Player owner, Location vaultLocation, boolean isVaultPublic) {
        this.vaultUuid = vaultUuid;
        this.owner = owner;
        this.vaultLocation = vaultLocation;
        this.isVaultPublic = isVaultPublic;
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
}
