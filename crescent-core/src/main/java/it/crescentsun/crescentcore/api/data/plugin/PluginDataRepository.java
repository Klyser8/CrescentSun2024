package it.crescentsun.crescentcore.api.data.plugin;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import static it.crescentsun.crescentcore.api.data.plugin.PluginData.crescentCore;

/**
 * PluginDataRepository's focus is mainly to store instances of PluginData classes. These are uniquely identified through two means: <br>
 * - The class type of the PluginData being stored <br>
 * - The UUID of the PluginData instance (AKA the primary key that would be used in the DB) <br>
 * - The PluginData instance itself <br><br>
 * While this class isn't meant to be used, it's a crucial part of the PluginData API and may be useful to see for reference/debugging.
 *
 */
public class PluginDataRepository {
    //Caches for reflection metadata
    private final Map<Class<? extends PluginData>, String> tableNameFromClassCache = new HashMap<>();
    private final Map<String, Class<? extends PluginData>> classFromTableNameCache = new HashMap<>();

    private final Map<Class<? extends PluginData>, Pair<Field, DatabaseColumn>> primaryKeyCache = new HashMap<>();
    private final Map<Class<? extends PluginData>, Map<Field, DatabaseColumn>> serializableFieldsCache = new HashMap<>();

    // This map stores data for each PluginData class. Each class has a map of key-value pairs.
    // First value stores the class which is used to store the data.
    // Second value stores the UUID used to fetch the data instance
    // Third is the data instance.
    private final Map<PluginDataIdentifier, PluginData> repository = new HashMap<>();

    /**
     * Registers a new class to the repository, allowing it to store instances.
     * Its scope is internal, as it is meant to be called by {@link PluginDataRegistry#registerDataClass(JavaPlugin, Class)}
     *
     * @param classType The class to register. Must extend PluginData.
     */
    protected  <T extends PluginData> void registerNew(Class<T> classType) {
        primaryKeyCache.put(classType, findPrimaryKeyField(classType));
        serializableFieldsCache.put(classType, findSerializableFields(classType));

        String tableName = findTableName(classType);
        if (tableName != null) {
            tableNameFromClassCache.put(classType, tableName);
            classFromTableNameCache.put(tableName, classType);
        }
    }

    /**
     * Adds an instance of a PluginData class to the repository.
     *
     * @param classType The class type.
     * @param uuid   The uuid identifying this particular instance.
     * @param value The instance of the class to add.
     */
    public <T extends PluginData> void addDataInstance(@NotNull Class<T> classType, @NotNull UUID uuid, @NotNull PluginData value) {
        // Check if the value already exists
        if (getData(classType, uuid) != null) {
            crescentCore.getLogger().warning("Replaced duplicate data instance: " + value + ". Was this intentional?");
        }
        if (primaryKeyCache.get(classType) == null) {
            throw new IllegalArgumentException("Error adding data instance: Class not registered: " + classType.getName());
        }
        PluginDataIdentifier identifier = new PluginDataIdentifier(classType, uuid);
        repository.put(identifier, value);
    }


    /**
     * Retrieves the specified instance, given a class type and uuid.
     * Always returns a PluginData instance, or throws an exception if the class is not registered.
     *
     * @param classType The class type.
     * @param uuid The uuid tied to the instance to retrieve.
     * @return The instance associated with the given uuid, or null if no instance was found.
     * @throws IllegalArgumentException If the class is not registered.
     */
    @SuppressWarnings("unchecked")
    @Nullable public <T extends PluginData> T getData(@NotNull Class<T> classType, @NotNull UUID uuid) {
        if (primaryKeyCache.get(classType) == null) {
            throw new IllegalArgumentException("Error getting data instance: Class not registered: " + classType.getName());
        }
        PluginDataIdentifier identifier = new PluginDataIdentifier(classType, uuid);
        return (T) repository.get(identifier);
    }

    /**
     * Retrieves all instances of a particular PluginData class.
     * Always returns a map of PluginData instances, or throws an exception if the class is not registered.
     *
     * @param classType The class type.
     * @return A map of all key-value pairs for the given class.
     * @throws IllegalArgumentException If the class is not registered.
     */
    @SuppressWarnings("unchecked")
    public <T extends PluginData> Map<UUID, T> getAllDataOfType(Class<T> classType) {
        Map<UUID, T> result = new HashMap<>();

        for (Map.Entry<PluginDataIdentifier, PluginData> entry : repository.entrySet()) {
            if (entry.getKey().classType().equals(classType)) {
                result.put(entry.getKey().uuid(), (T) entry.getValue());
            }
        }
        return result;
    }

    /**
     * Retrieves all instances of PluginData stored in the repository.
     * The map is immutable and cannot be modified.
     * @return An immutable map of all PluginData instances.
     */
    public Map<PluginDataIdentifier, PluginData> getAllData() {
        return ImmutableMap.copyOf(repository);
    }


    /**
     * Removes an instance of a PluginData class from the repository.
     *
     * @param classType The class type.
     * @param uuid The uuid identifying the instance to remove.
     * @return The removed instance, or null if no instance was found for the given uuid.
     */
    @SuppressWarnings("unchecked")
    public <T extends PluginData> T removeData(Class<T> classType, UUID uuid) {
        if (primaryKeyCache.get(classType) == null) {
            throw new IllegalArgumentException("Error removing data instance: Class not registered: " + classType.getName());
        }
        PluginDataIdentifier identifier = new PluginDataIdentifier(classType, uuid);
        return (T) repository.remove(identifier);
    }

    /**
     * Clears all data associated with a specific PluginData class.
     *
     * @param classType The class type.
     */
    @ApiStatus.Internal
    public void clearData(Class<? extends PluginData> classType) {
        repository.entrySet().removeIf(entry -> entry.getKey().classType().equals(classType));
    }

    @ApiStatus.Internal
    public void clearAllData() {
        repository.clear();
    }

    /**
     * Retrieves the table name for the given plugin data class.
     * The table name is constructed using the plugin's simple name and the table name specified in the DatabaseTable annotation.
     *
     * @param dataClass The class of the plugin data.
     * @return The table name in the format "pluginName_tableName", or null if the class is not annotated with DatabaseTable.
     */
    public static String getTableNameFromPluginDataClass(Class<? extends PluginData> dataClass) {
        return crescentCore.getPluginDataRepository().tableNameFromClassCache.get(dataClass);
    }

    /**
     * Retrieves the plugin data class corresponding to the given full table name.
     * The full table name is expected to be in the format "pluginName_tableName".
     *
     * @param fullTableName The full table name in the format "pluginName_tableName".
     * @return The class of the plugin data that matches the given full table name, or null if no match is found.
     */
    public static Class<? extends PluginData> getPluginDataClassFromFullTableName(String fullTableName) {
        return crescentCore.getPluginDataRepository().classFromTableNameCache.get(fullTableName);
    }

    /**
     * Retrieves the primary key field and its corresponding DatabaseColumn annotation from the given plugin data class.
     *
     * @param dataClass The class of the plugin data.
     * @return A Pair containing the primary key field and its DatabaseColumn annotation, or null if no primary key is found.
     */
    public static Pair<Field, DatabaseColumn> getPrimaryKeyField(Class<? extends PluginData> dataClass) {
        return crescentCore.getPluginDataRepository().primaryKeyCache.get(dataClass);
    }

    @NotNull
    public static Map<Field, DatabaseColumn> getSerializableFields(Class<? extends PluginData> dataClass) {
        return crescentCore.getPluginDataRepository().serializableFieldsCache.get(dataClass);
    }

    private static Pair<Field, DatabaseColumn> findPrimaryKeyField(Class<? extends PluginData> dataClass) {
        Field[] fields = dataClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(DatabaseColumn.class)) {
                DatabaseColumn annotation = field.getAnnotation(DatabaseColumn.class);
                if (annotation.isPrimaryKey()) {
                    return Pair.of(field, annotation);
                }
            }
        }
        throw new IllegalStateException("Plugin data class " + dataClass.getName() + " does not have a primary key field! Please add one.");
    }

    private static Map<Field, DatabaseColumn> findSerializableFields(Class<? extends PluginData> dataClass) {
        List<Pair<Field, DatabaseColumn>> fieldsList = new ArrayList<>();
        Field[] fields = dataClass.getDeclaredFields();

        // Collect fields and their annotations
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(DatabaseColumn.class)) {
                DatabaseColumn annotation = field.getAnnotation(DatabaseColumn.class);
                fieldsList.add(Pair.of(field, annotation));
            }
        }

        // Sort the fields based on the `order` attribute in the annotation
        fieldsList.sort(Comparator.comparingInt(pair -> pair.value().order()));

        // Use LinkedHashMap to preserve the sorted order
        Map<Field, DatabaseColumn> fieldsMap = new LinkedHashMap<>();
        for (Pair<Field, DatabaseColumn> pair : fieldsList) {
            fieldsMap.put(pair.key(), pair.value());
        }

        return fieldsMap;
    }


    private static String findTableName(Class<? extends PluginData> dataClass) {
        if (!dataClass.isAnnotationPresent(DatabaseTable.class)) {
            crescentCore.getLogger().warning("Plugin data class " + dataClass.getName() + " does not have a DatabaseTable annotation! Skipping it.");
            return null;
        }
        DatabaseTable annotation = dataClass.getAnnotation(DatabaseTable.class);
        String fullTableName = annotation.plugin().getSimpleName() + "_" + annotation.tableName();
        return fullTableName.toLowerCase();
    }
}
