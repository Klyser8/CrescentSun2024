package it.crescentsun.crescentcore.api.data.player;

import com.google.common.collect.ImmutableMap;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.DataEntry;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to register additional data that can be stored in the PlayerData class.
 * If you want your plugin to store data through KlyNetCore, call the registerAdditionalData
 * method in your onEnable method for each piece of data you want to store.
 * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
 */
public final class PlayerDataRegistry {

    public PlayerDataRegistry() {}
    private static boolean isFrozen = false;
    //Namespace is the table name (plugin name), key is the column name, and the DataEntry (value) is the data.
    private final Map<NamespacedKey, DataEntry<?>> playerDataRegistry = new HashMap<>();

    /**
     * Registers additional data to be stored in the PlayerData class, with a default value.
     * This method should be called in the onEnable method of your plugin.
     * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
     *
     * @param namespacedKey The namespaced key for the data
     * @param defaultValue The default value to use if the data is not present
     * @param <V> The type of the data
     */
    public <V> void registerPlayerDataEntry(NamespacedKey namespacedKey, DataType type, V defaultValue) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The player data registry is frozen and cannot be modified.");
        }
        namespacedKey = new NamespacedKey(namespacedKey.getNamespace(), namespacedKey.getKey());
        if (!type.getTypeClass().isInstance(defaultValue)) {
            throw new IllegalArgumentException("The default value for player data " + namespacedKey +
                    " does not match the type" + type + "!");
        }
        namespacedKey = new NamespacedKey(namespacedKey.getNamespace() + "_player_data", namespacedKey.getKey());
        DataEntry<V> data = new DataEntry<>(type, defaultValue);
        playerDataRegistry.put(namespacedKey, data);
        CrescentCore.getInstance().getLogger().info("Player data entry registered: " + namespacedKey);
    }

    /**
     * Returns an immutable version of the player data registry.
     * @return an immutable version of the player data registry.
     */
    public Map<NamespacedKey, DataEntry<?>> getPlayerDataRegistry() {
        return ImmutableMap.copyOf(playerDataRegistry);
    }

    /**
     * Returns a deep copy of the additional data registry.
     * @return A deep copy of the additional data registry
     */
    public Map<NamespacedKey, DataEntry<?>> clonePlayerDataEntryRegistry() {
        Map<NamespacedKey, DataEntry<?>> playerDataMap = new HashMap<>();
        for (NamespacedKey key : playerDataRegistry.keySet()) {
            NamespacedKey newKey = new NamespacedKey(key.namespace(), key.value());
            DataEntry<?> newData = new DataEntry<>(playerDataRegistry.get(key));
            playerDataMap.put(newKey, newData); // Use the copy constructor
        }
        return playerDataMap;
    }

    /**
     * Freezes the additional data registry, to prevent further modification once all plugins have been enabled.
     */
    public static void freezeRegistries() {
        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Player Data Registry frozen! " +
                "No more player data can be registered.");
    }

    /**
     * Returns a list of additional data registered by a specific plugin.
     *
     * @param namespace The namespace of the plugin
     * @return A list of additional data registered by the plugin
     */
    public Map<NamespacedKey, DataEntry<?>> getPlayerDataEntryForNamespace(String namespace) {
        Map<NamespacedKey, DataEntry<?>> data = new HashMap<>();
        for (NamespacedKey namespacedKey : playerDataRegistry.keySet()) {
            if (namespacedKey.namespace().equalsIgnoreCase(namespace)) {
                data.put(namespacedKey, playerDataRegistry.get(namespacedKey));
            }
        }
        return data;
    }
}
