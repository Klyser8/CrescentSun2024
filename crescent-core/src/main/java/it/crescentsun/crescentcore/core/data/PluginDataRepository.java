package it.crescentsun.crescentcore.core.data;

import it.crescentsun.crescentcore.api.data.plugin.PluginData;

import java.util.HashMap;
import java.util.Map;

public class PluginDataRepository {

    // This map stores data for each PluginData class. Each class has a map of key-value pairs.
    // First value stores the class which is used to store the data.
    // Second value stores the key used to fetch the data instance/
    // Third is the data instance.
    private final Map<Class<?>, Map<Object, PluginData>> dataStore = new HashMap<>();

    /**
     * Registers a new class to the repository, allowing it to store instances.
     *
     * @param clazz The class to register.
     * @param <K>   The type of the key used to identify instances.
     * @param <V>   The type of the value (instances of the PluginData class).
     */
    public <K, V> void registerNew(Class<V> clazz) {
        dataStore.putIfAbsent(clazz, new HashMap<>());
    }

    /**
     * Adds an instance of a PluginData class to the repository.
     *
     * @param clazz The class type.
     * @param key   The key identifying this particular instance.
     * @param value The instance of the class to add.
     */
    public void addData(Class<?> clazz, Object key,  PluginData value) {
        Map<Object, PluginData> classData = dataStore.get(clazz);
        if (classData != null) {
            classData.put(key, value);
        } else {
            throw new IllegalArgumentException("Class not registered: " + clazz.getName());
        }
    }

    /**
     * Retrieves an instance of a PluginData class from the repository.
     *
     * @param clazz The class type.
     * @param key   The key identifying the instance to retrieve.
     * @return The instance associated with the given key.
     */
    @SuppressWarnings("unchecked")
    public PluginData getData(Class<?> clazz, Object key) {
        Map<Object, PluginData> classData = dataStore.get(clazz);
        if (classData != null) {
            return classData.get(key);
        } else {
            throw new IllegalArgumentException("Class not registered: " + clazz.getName());
        }
    }

    /**
     * Retrieves all instances of a particular PluginData class.
     *
     * @param clazz The class type.
     * @return A map of all key-value pairs for the given class.
     */
    @SuppressWarnings("unchecked")
    public Map<Object, PluginData> getAllData(Class<?> clazz) {
        Map<Object, PluginData> classData = dataStore.get(clazz);
        if (classData != null) {
            return classData; // Cast the map to the correct type
        } else {
            throw new IllegalArgumentException("Class not registered: " + clazz.getName());
        }
    }

    /**
     * Removes an instance of a PluginData class from the repository.
     *
     * @param clazz The class type.
     * @param key   The key identifying the instance to remove.
     * @return The removed instance, or null if no instance was found for the given key.
     */
    @SuppressWarnings("unchecked")
    public PluginData removeData(Class<?> clazz, Object key) {
        Map<Object, PluginData> classData = dataStore.get(clazz);
        if (classData != null) {
            return classData.remove(key);
        } else {
            throw new IllegalArgumentException("Class not registered: " + clazz.getName());
        }
    }

    /**
     * Clears all data associated with a specific PluginData class.
     *
     * @param clazz The class type.
     */
    public void clearData(Class<?> clazz) {
        dataStore.remove(clazz);
    }
}
