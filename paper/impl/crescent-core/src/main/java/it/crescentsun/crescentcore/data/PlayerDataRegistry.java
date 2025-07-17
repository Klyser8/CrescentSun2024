package it.crescentsun.crescentcore.data;

import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.player.PlayerDataRegistryService;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.NamespacedKey;

import java.util.*;

/**
 * This class is used to register additional data that can be stored in the PlayerData class.
 * If you want your plugin to store data through KlyNetCore, call the registerAdditionalData
 * method in your onEnable method for each piece of data you want to store.
 * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
 */
public final class PlayerDataRegistry implements PlayerDataRegistryService {

    public PlayerDataRegistry() {}
    private boolean isFrozen = false;
    //Namespace is the table name (plugin name), key is the column name, and the DataEntry (value) is the data.
    private final Map<NamespacedKey, DataDefinition<?>> playerDataRegistry = new HashMap<>();

    /**
     * Registers additional data to be stored in the PlayerData class, with a default value.
     * This method should be called in the onEnable method of your plugin.
     * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
     *
     * @param namespacedKey The namespaced key for the data
     * @param type The DataType that is expected to be stored
     * @param persistent Whether the data should be stored in the database or deleted on server shutdown
     * @param defaultValue The default value to use if the data is not present
     */
    @Override
    public <V> void registerDataDefinition(NamespacedKey namespacedKey, DataType type, boolean persistent, V defaultValue) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The player data registry is frozen and cannot be modified.");
        }
        if (!type.getTypeClass().isInstance(defaultValue)) {
            throw new IllegalArgumentException("The default value for player data " + namespacedKey +
                    " does not match the type " + type + "!");
        }
        DataDefinition<V> definition = new DataDefinition<>(type, defaultValue, persistent);
        playerDataRegistry.put(namespacedKey, definition);
        CrescentCore.getInstance().getLogger().info("Player data definition registered: " + namespacedKey);
    }


    /**
     * Returns an immutable version of the player data registry.
     * @return an immutable version of the player data registry.
     */
    @Override
    public Map<NamespacedKey, DataDefinition<?>> getRegistry() {
        return Collections.unmodifiableMap(playerDataRegistry);
    }

    public void freezeRegistry() {
        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Player Data Registry frozen! " +
                "No more player data can be registered.");
    }

    @Override
    public Map<NamespacedKey, DataDefinition<?>> getPlayerDataDefinitionsForPlugin(String plugin) {
        Map<NamespacedKey, DataDefinition<?>> data = new HashMap<>();
        for (NamespacedKey namespacedKey : playerDataRegistry.keySet()) {
            if (namespacedKey.namespace().equalsIgnoreCase(plugin)) {
                data.put(namespacedKey, playerDataRegistry.get(namespacedKey));
            }
        }
        return data;
    }

    @Override
    public boolean isRegistryFrozen() {
        return isFrozen;
    }

    /**
     * @return a list of all the plugin names that have registered player data.
     */
    public List<String> getRegisteredPluginNames() {
        List<String> pluginNames = new ArrayList<>();
        for (NamespacedKey key : playerDataRegistry.keySet()) {
            if (!pluginNames.contains(key.namespace())) {
                pluginNames.add(key.namespace());
            }
        }
        return pluginNames;
    }

}
