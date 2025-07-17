package it.crescentsun.api.crescentcore.data.player;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The PlayerDataService is used to register additional data that can be stored in the PlayerData class.
 * If you want your plugin to store data through the crescent-core-api, use this API appropriately.
 *
 * @see PlayerData
 * @see PlayerDataRegistryService
 */
public interface PlayerDataService {

    /**
     * Returns the PlayerData object for the given player UUID.
     * @param playerUuid The UUID of the player
     * @return The PlayerData object for the given player UUID
     */
    PlayerData getData(UUID playerUuid);

    /**
     * @see #getData(UUID)
     * @param player The player whose data you want to retrieve
     * @return The PlayerData object for the given player
     */
    default PlayerData getData(Player player) {
        return getData(player.getUniqueId());
    }
    /**
     * @return An immutable list of all PlayerData objects
     */
    List<PlayerData> getAllData();

    /**
     * Saves the PlayerData object asynchronously, for the given player UUID to the database.
     * @param playerUuid The UUID of the player =
     * @return The PlayerData object for the given player UUID
     */
    CompletableFuture<PlayerData> saveDataAsync(UUID playerUuid);
    /**
     * @see #saveDataAsync(UUID)
     * @param player The player whose data you want to save
     * @return The PlayerData object for the given player
     */
    default CompletableFuture<PlayerData> saveDataAsync(Player player) {
        return saveDataAsync(player.getUniqueId());
    }
    /**
     * @see #saveDataAsync(UUID)
     * @param playerData The PlayerData object you want to save
     * @return The PlayerData object for the given player
     */
    default CompletableFuture<PlayerData> saveDataAsync(PlayerData playerData) {
        return saveDataAsync(playerData.getPlayer().getUniqueId());
    }

    /**
     * Loads the PlayerData object asynchronously, for the given player UUID from the database.
     * @param playerUuid The UUID of the player
     * @return The PlayerData object for the given player UUID
     */
    CompletableFuture<PlayerData> loadDataAsync(UUID playerUuid);

    /**
     * Removes the PlayerData object for the given player UUID from the player data repository.
     * This doesn't delete the data from the database.
     * @param playerUuid The UUID of the player
     * @return The PlayerData object for the given player UUID which was removed
     */
    PlayerData removeData(UUID playerUuid);

    /**
     * Deletes the PlayerData object for the given player UUID from the player data repository AND the database.
     * @param playerUuid The UUID of the player
     */
    void deleteData(UUID playerUuid);

}
