package it.crescentsun.api.crystals;

import org.bukkit.Location;

import java.util.UUID;

public interface VaultService {

    UUID[] getVaultsByOwner(UUID ownerUUID);

    UUID getClosestVaultUUID(Location location);

    UUID getVaultUUIDAtLocation(Location location);

}
