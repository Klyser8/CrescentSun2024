package it.crescentsun.api.crescentcore.data.player;

import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataType;
import org.bukkit.NamespacedKey;

import java.util.Map;

/**
 * Service for registering additional data that can be stored in a player's {@link PlayerData} object.
 *
 * @see PlayerData
 * @see PlayerDataService
 */
public interface PlayerDataRegistryService {

    /**
     * Registers a new DataDefinition to be used throughout the network.
     * This method should be called in the onEnable method of your plugin.
     * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
     *
     * @param namespacedKey The namespaced key for the data
     * @param type The DataType that is expected to be stored
     * @param persistent Whether the data should be stored in the database or deleted on server shutdown
     * @param defaultValue The default value to use if the data is not present
     * @see DataDefinition
     */
    <V> void registerDataDefinition(NamespacedKey namespacedKey, DataType type, boolean persistent, V defaultValue);

    /**
     * Returns an immutable version of the player data registry.
     * Thi
     * @return an immutable version of the player data registry.
     */
    Map<NamespacedKey, DataDefinition<?>> getRegistry();

    /**
     * Returns whether the registry is frozen and can no longer be modified.
     * If an attempt to modify the registry is made after it has been frozen, an UnsupportedOperationException will be thrown.
     * @return Whether the registry is frozen
     */
    boolean isRegistryFrozen();

    /**
     * Returns a list of player data definitions registered by a specific plugin.
     *
     * @param pluginName The name of the plugin to get the data entries for
     * @return A list of the player data definitions registered by the plugin
     */
    Map<NamespacedKey, DataDefinition<?>> getPlayerDataDefinitionsForPlugin(String pluginName);

}
