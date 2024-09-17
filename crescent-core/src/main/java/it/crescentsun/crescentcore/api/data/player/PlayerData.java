package it.crescentsun.crescentcore.api.data.player;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.event.player.PlayerDataFetchEvent;
import it.crescentsun.crescentcore.api.event.player.PlayerDataUpdateEvent;
import it.crescentsun.crescentcore.api.registry.CrescentNamespacedKeys;
import it.crescentsun.crescentcore.api.data.DataEntry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;

import static it.crescentsun.crescentcore.CrescentCore.PLAYER_DATA_ENTRY_REGISTRY;

/**
 * Class used to store player data throughout the network.
 * To register data entries to be stored on the database and be accessible throughout the network,
 * see {@link PlayerDataRegistry}.
 */
public class PlayerData {
    private final Map<NamespacedKey, DataEntry<?>> playerDataEntries;
    private final Player player;

    @ApiStatus.Internal
    public PlayerData(Player player) {
        this.player = player;
        playerDataEntries = PLAYER_DATA_ENTRY_REGISTRY.clonePlayerDataEntryRegistry();
    }

    /**
     * @return The unique ID of the player.
     */
    public UUID getUniqueId() {
        return UUID.fromString(getDataValue(CrescentNamespacedKeys.PLAYER_UUID));
    }

    /**
     * Returns the PlayerDataEntry object for the given namespaced key.
     * This method is solely used in database operations, though it can be used for debugging purposes.
     *
     * @param namespacedKey The namespaced key of the data to retrieve
     * @return The PlayerDataEntry object for the given namespaced key
     */
    @ApiStatus.Internal
    public DataEntry<?> getDataEntry(NamespacedKey namespacedKey) {
        return playerDataEntries.get(namespacedKey);
    }

    /**
     * Returns the value of the additional data with the given namespaced key.
     * To avoid complications or possible data corruption, the method may return null if:
     * - The data is not found
     * - The data is found but the value is not of the expected type
     * The second case is unlikely to happen, as an error will be thrown on data registration instead, but is still there for safety.
     * <br>
     * <br>
     * To use the data obtained through this method, you should cast it to the expected type. If you're not sure of the type to be used,
     * you can use the method {@link #getDataEntry(NamespacedKey)} to get the PlayerDataEntry object and check its type with {@link DataEntry#getType()}.
     *
     * @param namespacedKey The namespaced key of the data to retrieve
     * @return The value of the additional data with the given namespaced key, or null if there were issues retrieving the data.
     * @param <V> The type of the data.
     */
    @SuppressWarnings("unchecked")
    public <V> V getDataValue(NamespacedKey namespacedKey) {
        if (!namespacedKey.getNamespace().contains("_player_data")) {
            namespacedKey = new NamespacedKey(namespacedKey.getNamespace() + "_player_data", namespacedKey.getKey());
        }
        DataEntry<?> data = playerDataEntries.get(namespacedKey);
        if (data == null) {
            CrescentCore.getInstance().getLogger().warning(
                    "Player data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] not found when retrieving!");
            return null;
        }
        V value;
        try {
            value = (V) data.getType().getTypeClass().cast(data.getValue());
        } catch (ClassCastException e) {
            CrescentCore.getInstance().getLogger().warning(
                    "Failed to cast the value of additional data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "]!");
            return null;
        }
        PlayerDataFetchEvent event = new PlayerDataFetchEvent(this, namespacedKey, value, !Bukkit.isPrimaryThread());
        event.callEvent();
        return value;
    }

    /**
     * Updates the value of the additional data with the given namespaced key.
     * Once again, to avoid complications or possible data corruption, the method may throw an error if:
     * - The namespaced key is not linked to any data
     * - The provided value is of a mismatched type
     * <br>
     * <br>
     * It's important to note that this method will trigger the {@link PlayerDataUpdateEvent} event, which can be cancelled.
     * In case the event is cancelled, the data will not be updated.
     *
     * @param namespacedKey The namespaced key of the data to update
     * @param value The new value to set. Must be of the same type as the original value.
     * @param <V> The type of the data.
     */
    public <V> void updateDataValue(NamespacedKey namespacedKey, V value) {
        if (!namespacedKey.getNamespace().contains("_player_data")) {
            namespacedKey = new NamespacedKey(namespacedKey.getNamespace() + "_player_data", namespacedKey.getKey());
        }
        DataEntry<?> data = playerDataEntries.get(namespacedKey);
        if (data == null) {
            CrescentCore.getInstance().getLogger().warning(
                    "Player data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] not found when updating!");
            return;
        }
        if (!data.getType().getTypeClass().isInstance(value)) {
            throw new IllegalArgumentException("Player data " + namespacedKey.namespace() + ":" + namespacedKey.value() +
                    " was provided with an incorrect value! Expected " + data.getType().toString());
        }
        PlayerDataUpdateEvent event = new PlayerDataUpdateEvent(this, namespacedKey, value, !Bukkit.isPrimaryThread());
        event.callEvent();
        if (!event.isCancelled()) {
            data.setValue(event.getValue());
        }
    }

    /**
     * @return The player object associated with this data.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Should only be used for debugging purposes.
     * @return an immutable version of the player data registry.
     */
    public Map<NamespacedKey, DataEntry<?>> getAllDataEntries() {
        return Map.copyOf(playerDataEntries);
    }

}