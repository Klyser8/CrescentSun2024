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
     * Returns the PlayerData object for the given player UUID. The returned object may only be read or
     * mutated on the Paper server thread because its accessors dispatch Bukkit events.
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
     * Captures persistent values on the Paper server thread, writes that snapshot asynchronously, then
     * completes with the current cached object on the Paper server thread. The future completes
     * exceptionally if snapshotting, persistence, or publication of the result fails.
     * @param playerUuid The UUID of the player
     * @return A future containing the saved player's cached data, or {@code null} if it was not cached
     */
    CompletableFuture<PlayerData> saveDataAsync(UUID playerUuid);
    /**
     * @see #saveDataAsync(UUID)
     * @param player The player whose data you want to save
     * @return A future containing the saved player's cached data
     */
    default CompletableFuture<PlayerData> saveDataAsync(Player player) {
        return saveDataAsync(player.getUniqueId());
    }
    /**
     * @see #saveDataAsync(UUID)
     * @param playerData The PlayerData object you want to save
     * @return A future containing the saved player's cached data
     */
    default CompletableFuture<PlayerData> saveDataAsync(PlayerData playerData) {
        return saveDataAsync(playerData.getPlayer().getUniqueId());
    }

    /**
     * Reads player values asynchronously, then creates and publishes the PlayerData object on the Paper
     * server thread. The future completes exceptionally if the database read or server-thread publication fails.
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
