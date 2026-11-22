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
    private volatile boolean isFrozen = false;
    //Namespace is the table name (plugin name), key is the column name, and the DataEntry (value) is the data.
    private volatile Map<NamespacedKey, DataDefinition<?>> playerDataRegistry = new LinkedHashMap<>();
    private volatile Map<String, Map<NamespacedKey, DataDefinition<?>>> definitionsByPlugin = Map.of();

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
        Objects.requireNonNull(namespacedKey, "Player data key cannot be null");
        Objects.requireNonNull(type, "Player data type cannot be null for " + namespacedKey);
        if (defaultValue == null ? !isNullable(type) : !type.getTypeClass().isInstance(defaultValue)) {
            throw new IllegalArgumentException("The default value for player data " + namespacedKey +
                    " does not match the type " + type + "!");
        }
        DataDefinition<V> definition = new DataDefinition<>(type, defaultValue, persistent);
        if (playerDataRegistry.putIfAbsent(namespacedKey, definition) != null) {
            throw new IllegalArgumentException("Duplicate player data key registered: " + namespacedKey);
        }
        CrescentCore.getInstance().getLogger().info("Player data definition registered: " + namespacedKey);
    }


    /**
     * Returns an immutable version of the player data registry.
     * @return an immutable version of the player data registry.
     */
    @Override
    public Map<NamespacedKey, DataDefinition<?>> getRegistry() {
        return isFrozen ? playerDataRegistry : Collections.unmodifiableMap(playerDataRegistry);
    }

    public void freezeRegistry() {
        Map<String, Map<String, NamespacedKey>> columnsByPlugin = new HashMap<>();
        Map<String, Map<NamespacedKey, DataDefinition<?>>> groupedDefinitions = new LinkedHashMap<>();
        for (Map.Entry<NamespacedKey, DataDefinition<?>> entry : playerDataRegistry.entrySet()) {
            NamespacedKey key = entry.getKey();
            String pluginName = key.namespace().toLowerCase(Locale.ROOT);
            validateSqlIdentifier("player_data_" + pluginName, "player-data table", key.toString());
            String columnName = key.getKey().toLowerCase(Locale.ROOT);
            validateSqlIdentifier(columnName, "player-data column", key.toString());

            NamespacedKey previous = columnsByPlugin
                    .computeIfAbsent(pluginName, ignored -> new HashMap<>())
                    .putIfAbsent(columnName, key);
            if (previous != null) {
                throw new IllegalStateException("Player data keys " + previous + " and " + key
                        + " produce the same database column '" + columnName + "' in plugin '" + pluginName + "'.");
            }
            groupedDefinitions.computeIfAbsent(pluginName, ignored -> new LinkedHashMap<>())
                    .put(key, entry.getValue());
        }

        Map<String, Map<NamespacedKey, DataDefinition<?>>> immutableGroups = new LinkedHashMap<>();
        groupedDefinitions.forEach((plugin, definitions) -> immutableGroups.put(
                plugin, Collections.unmodifiableMap(new LinkedHashMap<>(definitions))));
        definitionsByPlugin = Collections.unmodifiableMap(immutableGroups);
        playerDataRegistry = Collections.unmodifiableMap(new LinkedHashMap<>(playerDataRegistry));
        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Player Data Registry frozen! " +
                "No more player data can be registered.");
    }

    @Override
    public Map<NamespacedKey, DataDefinition<?>> getPlayerDataDefinitionsForPlugin(String plugin) {
        if (isFrozen) {
            return definitionsByPlugin.getOrDefault(plugin.toLowerCase(Locale.ROOT), Map.of());
        }
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
        Set<String> pluginNames = new LinkedHashSet<>();
        if (isFrozen) {
            pluginNames.addAll(definitionsByPlugin.keySet());
        } else {
            for (NamespacedKey key : playerDataRegistry.keySet()) {
                pluginNames.add(key.namespace());
            }
        }
        return List.copyOf(pluginNames);
    }

    private static boolean isNullable(DataType type) {
        return type == DataType.NULLABLE_VARCHAR_16
                || type == DataType.NULLABLE_VARCHAR_36
                || type == DataType.NULLABLE_VARCHAR_255;
    }

    private static void validateSqlIdentifier(String identifier, String kind, String owner) {
        if (identifier.length() > 64 || !identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalStateException("Invalid " + kind + " '" + identifier + "' registered by " + owner
                    + ". Database identifiers must match [A-Za-z_][A-Za-z0-9_]* and be at most 64 characters.");
        }
    }

}
