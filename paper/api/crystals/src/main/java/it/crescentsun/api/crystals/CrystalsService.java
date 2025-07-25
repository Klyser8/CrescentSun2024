package it.crescentsun.api.crystals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Service for managing crystals.
 */
public interface CrystalsService {

    /**
     * Spawn the chosen number of crystals as an item entity, with the specified animation.
     * The owner indicates who the crystals are spawned for, and who has priority to pick them up.
     * If the owner is null, the crystals are spawned for anyone to pick up.
     *
     * @param owner The player who the crystals are spawned for. Null if the crystals are spawned for anyone.
     * @param amount The number of crystals to spawn.
     * @param source The source of the crystals.
     * @param spawnAnimation The animation to play when the crystals are spawned.
     * @param spawnLocation The location to spawn the crystals at. Null if the player's location should be used.
     */
    void spawnCrystals(Player owner, int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation);
    default void spawnCrystals(int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation) {
        spawnCrystals(null, amount, source, spawnAnimation, spawnLocation);
    }

    /**
     * Get the number of crystals spawned in the network for a specific player
     * This is done through the usage of the PlayerData API.
     *
     * @param player The player to get the crystals for.
     * @return The number of crystals the player has spawned
     */
    int getCrystalsSpawned(Player player);

    /**
     * Get the number of crystals in the vault of a specific player.
     * This is done through the usage of the PlayerData API.
     *
     * @param player The player to get the crystals for.
     * @return The number of crystals in the player's vault
     */
    int getCrystalsInVault(Player player);

    /**
     * Adds a specified number of crystals to the player's vault.
     *
     * @param player the player whose vault to add crystals to
     * @param amount the number of crystals to add
     */
    void addCrystalsToVault(Player player, int amount);

    /**
     * Removes a specified number of crystals from the player's vault.
     *
     * @param player the player whose vault to remove crystals from
     * @param amount the number of crystals to remove
     */
    void removeCrystalsFromVault(Player player, int amount);

    /**
     * Sets the player's vault crystals to the specified amount.
     *
     * @param player the player whose vault to set crystals for
     * @param amount the new total number of crystals in the vault
     */
    void setCrystalsInVault(Player player, int amount);

}
