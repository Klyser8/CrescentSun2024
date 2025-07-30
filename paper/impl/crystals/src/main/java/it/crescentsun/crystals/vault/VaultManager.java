package it.crescentsun.crystals.vault;

import it.crescentsun.api.crescentcore.data.plugin.AbstractPluginDataManager;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataIdentifier;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.api.crescentcore.util.VectorUtils;
import it.crescentsun.api.crystals.VaultService;
import it.crescentsun.api.crystals.event.CreateVaultEvent;
import it.crescentsun.crystals.Crystals;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VaultManager extends AbstractPluginDataManager<Crystals, VaultData> implements VaultService {

    public VaultManager(Crystals plugin, PluginDataService pluginDataService) {
        super(plugin, VaultData.class, pluginDataService);
    }

    public static Map<Vector3i, Material> vaultBlockOffsets = Map.of(
            new Vector3i(0, 0, 0), Material.LODESTONE,
            new Vector3i(1, 0, 0), Material.DIAMOND_BLOCK,
            new Vector3i(-1, 0, 0), Material.DIAMOND_BLOCK,
            new Vector3i(0, 0, 1), Material.DIAMOND_BLOCK,
            new Vector3i(0, 0, -1),  Material.DIAMOND_BLOCK
            );

    public VaultData createVault(Player owner, Location location, boolean isPublic) {
        if (owner == null || location == null) {
            return null;
        }
        UUID vaultUUID = UUID.randomUUID();
        CreateVaultEvent event = new CreateVaultEvent(vaultUUID, owner, location, isPublic);
        event.callEvent();
        if (event.isCancelled()) {
            return null;
        }
        VaultData vaultData = new VaultData(
                plugin,
                event.getVaultUuid(),
                event.getOwner().getUniqueId(),
                event.isVaultPublic(),
                plugin.getCrescentCoreAPI().getServerName(),
                event.getVaultLocation()
        );
        vaultData.tryInit();
        vaultData.saveAndSync();
        return vaultData;
    }

    /**
     * Deletes the vault at the specified location, if it exists.
     *
     * @param location The location of the vault to delete.
     * @return The VaultData object that was deleted, or null if no vault was found at the location.
     */
    public VaultData deleteVault(@NotNull Location location) {
        VaultData vaultData = getVaultAtLocation(location);
        if (vaultData == null) {
            return null;
        }
        CompletableFuture<PluginDataIdentifier<PluginData>> future = vaultData.deleteAndSync();
        future.join(); // Wait for the deletion to complete
        return vaultData;
    }

    /**
     * Deletes the vault with the specified UUID, if it exists.
     *
     * @param vaultUUID The UUID of the vault to delete.
     * @return The VaultData object that was deleted, or null if no vault was found with the given UUID.
     */
    public VaultData deleteVault(@NotNull UUID vaultUUID) {
        VaultData vaultData = getDataInstance(vaultUUID);
        if (vaultData == null) {
            return null;
        }
        CompletableFuture<PluginDataIdentifier<PluginData>> future = vaultData.deleteAndSync();
        future.join(); // Wait for the deletion to complete
        return vaultData;
    }

    /**
     * Deletes all vaults owned by the specified owner UUID.
     *
     * @param ownerUUID The UUID of the owner whose vaults should be deleted.
     * @return An array of VaultData objects that were deleted or an empty array if no vaults were found.
     */
    public VaultData[] deleteVaultsOwnedBy(@NotNull UUID ownerUUID) {
        UUID[] vaultIds = getVaultsByOwner(ownerUUID);
        if (vaultIds.length == 0) {
            return new VaultData[0];
        }
        VaultData[] vaults = Arrays.stream(vaultIds)
                .map(this::getDataInstance)
                .filter(Objects::nonNull)
                .toArray(VaultData[]::new);

        List<CompletableFuture<PluginDataIdentifier<PluginData>>> futures = Arrays.stream(vaults)
                .map(VaultData::deleteAndSync)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // wait once

        return vaults;
    }

    /**
     * Gets the VaultData object found at the specified location, if any.
     *
     * @param location The location to check.
     * @return The VaultData object at the given location or null if none is found.
     */
    @Nullable
    public VaultData getVaultAtLocation(Location location) {
        return getAllData(true).stream()
                .filter(vault -> VectorUtils.isInSameBlockLocation(location, vault.getLocation()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public UUID getVaultUUIDAtLocation(Location location) {
        VaultData vault = getVaultAtLocation(location);
        return vault != null ? vault.getUuid() : null;
    }

    /**
     * Gets the closest Vault from the specified location. May be null if none is found in the world.
     * @param location The location to check.
     * @return The closest Vault to the location, or null if none is found.
     */
    @Nullable public VaultData getClosestVault(Location location) {
        return getAllData(true).stream()
                .min(Comparator.comparingDouble(vault -> location.distanceSquared(vault.getLocation())))
                .orElse(null);
    }

    @Override
    public UUID getClosestVaultUUID(Location location) {
        VaultData vault = getClosestVault(location);
        return vault != null ? vault.getUuid() : null;
    }

    /**
     * Check if the structure of a Vault is valid.
     * The structure, from the top-down view, should look like this: <br>
     * - D = Gold block <br>
     * - L = Lodestone <br>
     * - A = Anything <br>
     * A D A <br>
     * D L D <br>
     * A D A <br>
     *
     * @param location The location of the Vault AKA the lodestone
     * @return Whether the structure is valid or not.
     */
    public static boolean isVaultStructureValid(@NotNull Location location) {
        for (Map.Entry<Vector3i, Material> entry : vaultBlockOffsets.entrySet()) {
            Vector3i offset = entry.getKey();
            Material expectedMaterial = entry.getValue();
            Location blockLocation = location.clone().add(offset.x, offset.y, offset.z);
            if (blockLocation.getBlock().getType() != expectedMaterial) {
                return false; // If any block does not match the expected material, return false
            }
        }//
        return true;
    }

    @Override
    public UUID[] getVaultsByOwner(UUID ownerUUID) {
        return getAllData(true).stream()
                .filter(vault -> vault.getOwnerUUID().equals(ownerUUID))
                .map(VaultData::getUuid)
                .toArray(UUID[]::new);
    }
}
