package it.crescentsun.api.crystals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Service for managing crystals.
 */
public interface CrystalsService {

    /**
     * Spawn the chosen amount of crystals as an item entity, with the specified animation.
     * The owner indicates who the crystals are spawned for, and who has priority to pick them up.
     * If the owner is null, the crystals are spawned for anyone to pick up.
     *
     * @param owner The player who the crystals are spawned for. Null if the crystals are spawned for anyone.
     * @param amount The amount of crystals to spawn.
     * @param source The source of the crystals.
     * @param spawnAnimation The animation to play when the crystals are spawned.
     * @param spawnLocation The location to spawn the crystals at. Null if the player's location should be used.
     */
    void spawnCrystals(Player owner, int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation);
    default void spawnCrystals(int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation) {
        spawnCrystals(null, amount, source, spawnAnimation, spawnLocation);
    }

    /**
     * Add the specified amount of crystals to the player's balance.
     * The source indicates where the crystals are coming from.
     *
     * @param player The player to add the crystals to.
     * @param amount The amount of crystals to add.
     * @param source The source of the crystals.
     */
    void addCrystals(Player player, int amount, CrystalSource source);
    default void addCrystals(Player player, int amount) {
        addCrystals(player, amount, CrystalSource.COMMAND);
    }

    /**
     * Set the player's crystal balance to the specified amount.
     * The source indicates where the crystals are coming from.
     *
     * @param player The player to set the crystals for.
     * @param amount The amount of crystals to set.
     * @param source The source of the crystals.
     */
    void setCrystals(Player player, int amount, CrystalSource source);
    default void setCrystals(Player player, int amount) {
        setCrystals(player, amount, CrystalSource.COMMAND);
    }

    /**
     * Remove the specified amount of crystals from the player's balance.
     * The source indicates the reason for removal.
     *
     * @param player The player to remove the crystals from.
     * @param amount The amount of crystals to remove.
     * @param source The source of the removal.
     */
    void removeCrystals(Player player, int amount, CrystalSource source);
    default void removeCrystals(Player player, int amount) {
        removeCrystals(player, amount, CrystalSource.COMMAND);
    }

    /**
     * Get the amount of crystals the player has.
     * This is done through the usage of the PlayerData API.
     *
     * @param player The player to get the crystals for.
     * @return The amount of crystals the player has.
     */
    int getCrystals(Player player);

}
