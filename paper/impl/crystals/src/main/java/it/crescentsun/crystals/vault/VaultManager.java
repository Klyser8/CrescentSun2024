package it.crescentsun.crystals.vault;

import it.crescentsun.api.crescentcore.data.plugin.AbstractPluginDataManager;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.crystals.Crystals;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.UUID;

public class VaultManager extends AbstractPluginDataManager<Crystals, VaultData> {

    public VaultManager(Crystals plugin, PluginDataService pluginDataService) {
        super(plugin, VaultData.class, pluginDataService);
    }

    public static Vector3i[] diamondBlockOffsets = new Vector3i[]{
            new Vector3i(1, 0, 0),
            new Vector3i(-1, 0, 0),
            new Vector3i(0, 0, 1),
            new Vector3i(0, 0, -1),
    };

    public VaultData createVault(Player owner, Location location, boolean isPublic) {
        if (owner == null || location == null) {
            return null;
        }
        VaultData vaultData = new VaultData(
                plugin,
                UUID.randomUUID(),
                owner.getUniqueId(),
                isPublic,
                plugin.getCrescentCoreAPI().getServerName(),
                location
        );
        vaultData.tryInit();
        vaultData.saveAndSync();
        return vaultData;
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
        //Check whether the block at the current location is of type gold pressure plate
        if (!location.getBlock().getType().equals(Material.LODESTONE)) {
            return false;
        }
        // Check that there are gold blocks at offsets matching the grid above
        for (Vector3i offset : diamondBlockOffsets) {
            Location loc = location.clone().offset(offset.x, offset.y, offset.z).toLocation(location.getWorld());
            if (!loc.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                return false;
            }
        }
        return true;
    }
}
