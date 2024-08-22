package it.klynet.klynetcore.plugindata;

import com.google.common.collect.ImmutableMap;
import it.klynet.klynetcore.KlyNetCore;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to register additional data that can be stored in the PlayerData class.
 * If you want your plugin to store data through KlyNetCore, call the registerAdditionalData
 * method in your onEnable method for each piece of data you want to store.
 * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
 */
public final class PluginDataRegistry {

    public PluginDataRegistry() {}
    private static boolean isFrozen = false;
    private final Map<NamespacedKey, PluginData<?>> pluginDataRegistry = new HashMap<>();

    /**
     * Registers additional data to be stored in the PlayerData class, with a default value.
     * This method should be called in the onEnable method of your plugin.
     * Once all plugins in the server have been enabled, the registry will be frozen and cannot be modified further.
     *
     * @param namespacedKey The namespaced key for the data
     * @param defaultValue The default value to use if the data is not present
     * @param <V> The type of the data
     */
    public <V> void registerPluginData(NamespacedKey namespacedKey, DataType type, V defaultValue) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The additional data registry is frozen and cannot be modified.");
        }
        if (!type.getTypeClass().isInstance(defaultValue)) {
            throw new IllegalArgumentException("The default value for plugin data " + namespacedKey +
                    " does not match the type" + type + "!");
        }
        PluginData<V> data = new PluginData<>(type, defaultValue);
        pluginDataRegistry.put(namespacedKey, data);
        KlyNetCore.getInstance().getLogger().info("Plugin data registered: " + namespacedKey);
    }

    /**
     * Returns an immutable version of the plugin data registry.
     * @return an immutable version of the plugin data registry.
     */
    public Map<NamespacedKey, PluginData<?>> getPluginDataRegistry() {
        return ImmutableMap.copyOf(pluginDataRegistry);
    }

    /**
     * Returns a deep copy of the additional data registry.
     * @return A deep copy of the additional data registry
     */
    public Map<NamespacedKey, PluginData<?>> clonePluginDataRegistry() {
        Map<NamespacedKey, PluginData<?>> additionalDataMap = new HashMap<>();
        for (NamespacedKey key : pluginDataRegistry.keySet()) {
            NamespacedKey newKey = new NamespacedKey(key.namespace(), key.value());
            PluginData<?> newData = new PluginData<>(pluginDataRegistry.get(key));
            additionalDataMap.put(newKey, newData); // Use the copy constructor
        }
        return additionalDataMap;
    }

    /**
     * Freezes the additional data registry, to prevent further modification once all plugins have been enabled.
     */
    public static void freezeRegistries() {
        isFrozen = true;
        KlyNetCore.getInstance().getLogger().info("Additional Data Registry frozen! " +
                "No more additional data can be registered.");
    }

    /**
     * Returns a list of additional data registered by a specific plugin.
     *
     * @param namespace The namespace of the plugin
     * @return A list of additional data registered by the plugin
     */
    public Map<NamespacedKey, PluginData<?>> getPluginDataForNamespace(String namespace) {
        Map<NamespacedKey, PluginData<?>> data = new HashMap<>();
        for (NamespacedKey namespacedKey : pluginDataRegistry.keySet()) {
            if (namespacedKey.namespace().equalsIgnoreCase(namespace)) {
                data.put(namespacedKey, pluginDataRegistry.get(namespacedKey));
            }
        }
        return data;
    }
}
