package it.crescentsun.api.crescentcore.data.player;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.DataEntry;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.event.player.PlayerDataFetchEvent;
import it.crescentsun.api.crescentcore.event.player.PlayerDataUpdateEvent;
import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Class used to store player data throughout the network.
 * It's heavily recommended to store any player-tied data through the PlayerData API, as it allows for easy access and management.
 * Additionally, it is automatically handled by the database, so you don't have to worry about serialization or deserialization at any point.
 * <br>
 * To register data entries to be stored on the database and be accessible throughout the network,
 * see {@link PlayerDataRegistryService}.
 */
public class PlayerData {
    private final Map<NamespacedKey, DataEntry<?>> playerDataEntries = new HashMap<>();
    private final Player player;

    public PlayerData(Player player, Map<NamespacedKey, Object> databaseValues, PlayerDataRegistryService registry) {
        requireMainThread("construct PlayerData");
        this.player = player;

        for (Map.Entry<NamespacedKey, DataDefinition<?>> entry : registry.getRegistry().entrySet()) {
            NamespacedKey key = entry.getKey();
            DataDefinition<?> definition = entry.getValue();
            Object value = databaseValues.getOrDefault(key, definition.getDefaultValue());
            DataEntry<?> dataEntry = new DataEntry<>(definition.getType(), value);
            playerDataEntries.put(key, dataEntry);
        }
    }

    /**
 * @return The unique ID of the player.
     */
    public UUID getUniqueId() {
        return (UUID) getDataValue(DatabaseNamespacedKeys.PLAYER_UUID)
                .orElseThrow(() -> new DataNotFoundException("Player UUID not found when retrieving!"));
    }

    /**
     * Returns the value of the additional data with the given namespaced key.
     * To avoid complications or possible data corruption, the method may throw an exception if: <br>
     * - The data is not found <br>
     * - The data is found but the value is not of the expected type <br>
     * The second case is unlikely to happen, as an error will be thrown on data registration instead, but is still there for safety.
     * <br>
     * <br>
     * To use the data obtained through this method, you should cast it to the expected type.
     * <br>
     * It's important to note that this method will trigger the {@link PlayerDataFetchEvent} event. While this event is not cancellable,
     * it can be used to check the value of the data before it's retrieved.
     *
     * @param namespacedKey The namespaced key of the data to retrieve
     * @return An Optional containing the value of the additional data with the given namespaced key, or an empty Optional if there were issues retrieving the data.
     * @param <V> The type of the data.
     * @throws IllegalStateException if called off the Paper server thread
     */
    @NotNull public <V> Optional<V> getDataValue(NamespacedKey namespacedKey) {
        requireMainThread("read player data");
        DataEntry<?> data = playerDataEntries.get(namespacedKey);
        if (data == null) {
            throw new DataNotFoundException("Player data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] not found when retrieving!");
        }
        V value = castDataValue(namespacedKey, data);
        PlayerDataFetchEvent<V> event = new PlayerDataFetchEvent<>(this, namespacedKey, value, false);
        event.callEvent();
        return Optional.ofNullable(event.getValue());
    }

    /**
     * Updates the value of the additional data with the given namespaced key.
     * Once again, to avoid complications or possible data corruption, the method may throw an error if: <br>
     * - The namespaced key is not linked to any data <br>
     * - The provided value is of a mismatched type <br>
     * It's important to note that this method will trigger the {@link PlayerDataUpdateEvent} event, which can be cancelled.
     * In case the event is cancelled, the data will not be updated.
     *
     * @param namespacedKey The namespaced key of the data to update
     * @param value The new value to set. Must be of the same type as the original value.
     * @param <V> The type of the data.
     * @throws IllegalStateException if called off the Paper server thread
     */
    public <V> void updateDataValue(NamespacedKey namespacedKey, V value) {
        requireMainThread("update player data");
        DataEntry<?> data = playerDataEntries.get(namespacedKey);
        if (data == null) {
            throw new DataNotFoundException("Player data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] not found when updating!");
        }

        Class<?> expectedClass = data.getType().getTypeClass();
        Object newValue = value;

        if (value == null && isNullable(data.getType())) {
            newValue = null;
        } else if (!expectedClass.isInstance(value)) {
            // Handle numeric type conversions
            if (Number.class.isAssignableFrom(expectedClass) && value instanceof Number) {
                newValue = convertNumber((Number) value, expectedClass);
            } else {
                throw new ClassCastException("Failed to cast the value of additional data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] when updating!");
            }
        }

        PlayerDataUpdateEvent event = new PlayerDataUpdateEvent(this, namespacedKey, newValue, false);
        event.callEvent();
        if (!event.isCancelled()) {
            data.setValue(event.getValue());
        }
    }


    @SuppressWarnings("unchecked")
    private <V> V castDataValue(NamespacedKey namespacedKey, DataEntry<?> data) {
        Object rawValue = data.getValue();
        Class<?> expectedClass = data.getType().getTypeClass();

        if (rawValue == null && isNullable(data.getType())) {
            return null;
        }

        if (!expectedClass.isInstance(rawValue)) {
            // Handle numeric type conversions
            if (Number.class.isAssignableFrom(expectedClass) && rawValue instanceof Number) {
                rawValue = convertNumber((Number) rawValue, expectedClass);
            } else {
                throw new ClassCastException("Failed to cast the value of additional data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] when fetching!");
            }
        }

        try {
            return (V) rawValue;
        } catch (ClassCastException e) {
            throw new ClassCastException("Failed to cast the value of additional data [" + namespacedKey.namespace() + ":" + namespacedKey.value() + "] when fetching!");
        }
    }

    private Object convertNumber(Number value, Class<?> targetType) {
        if (targetType == Integer.class) {
            return value.intValue();
        } else if (targetType == Long.class) {
            return value.longValue();
        } else if (targetType == Double.class) {
            return value.doubleValue();
        } else if (targetType == Float.class) {
            return value.floatValue();
        } else if (targetType == Short.class) {
            return value.shortValue();
        } else if (targetType == Byte.class) {
            return value.byteValue();
        } else {
            throw new IllegalArgumentException("Unsupported numeric type: " + targetType.getSimpleName());
        }
    }

    private static boolean isNullable(DataType type) {
        return type == DataType.NULLABLE_VARCHAR_16
                || type == DataType.NULLABLE_VARCHAR_36
                || type == DataType.NULLABLE_VARCHAR_255;
    }


    /**
     * @return The player object associated with this data.
     */
    public Player getPlayer() {
        requireMainThread("access the associated Bukkit player");
        return player;
    }

    /**
     * Should only be used for debugging purposes.
     * @return an immutable version of the player data registry.
     */
    public Map<NamespacedKey, DataEntry<?>> getAllDataEntries() {
        requireMainThread("inspect player data entries");
        return Map.copyOf(playerDataEntries);
    }

    /** Returns a stable value snapshot for database persistence without firing data-fetch events. */
    @ApiStatus.Internal
    public Map<NamespacedKey, Object> snapshotValuesForPersistence() {
        requireMainThread("snapshot player data for persistence");
        Map<NamespacedKey, Object> snapshot = new HashMap<>();
        playerDataEntries.forEach((key, entry) -> {
            Object value = entry.getValue();
            if (value instanceof Timestamp timestamp) {
                value = new Timestamp(timestamp.getTime());
            }
            snapshot.put(key, value);
        });
        return Collections.unmodifiableMap(snapshot);
    }

    private static void requireMainThread(String operation) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot " + operation + " off the Paper server thread.");
        }
    }

}
