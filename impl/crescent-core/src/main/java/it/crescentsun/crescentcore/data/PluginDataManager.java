package it.crescentsun.crescentcore.data;

import com.google.common.collect.ImmutableMap;
import it.crescentsun.api.crescentcore.data.ClassNotRegisteredException;
import it.crescentsun.api.crescentcore.data.plugin.*;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import it.unimi.dsi.fastutil.Pair;
import me.mrnavastar.protoweaver.api.netty.Sender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class PluginDataManager implements PluginDataService {
    private final Map<String, String> INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES = new HashMap<>();
    private final Map<String, String> SELECT_PLUGIN_DATA_QUERIES = new HashMap<>();

    private final Map<Class<? extends PluginData>, String> tableNameFromClassCache = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends PluginData>> classFromTableNameCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends PluginData>, Pair<Field, DatabaseColumn>> primaryKeyCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends PluginData>, Map<Field, DatabaseColumn>> serializableFieldsCache = new ConcurrentHashMap<>();

    private final Map<PluginDataIdentifier<? extends PluginData>, PluginData> repository = new ConcurrentHashMap<>();

    private final CrescentCore crescentCore;
    private final DatabaseManager dbManager;

    public PluginDataManager(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        dbManager = crescentCore.getDatabaseManager();
    }

    public void init() {
        populateInsertOrUpdatePluginDataQueries();
        populateSelectPluginDataQueries();
    }

    @Override
    public <T extends PluginData> CompletableFuture<T> asyncSaveDataAndSync(@NotNull T pluginData) {
        return CompletableFuture.supplyAsync(() -> saveData(pluginData)) // Start the async task
            .exceptionally(e -> {
                crescentCore.getLogger().severe("An error occurred while asynchronously saving plugin data to the database: " + e.getMessage());
                e.printStackTrace(); // Log the full stack trace for better debugging
                return null;
            }).thenApplyAsync(data -> {
                Sender send = crescentCore.getCrescentSunConnection().send(crescentCore.getPluginDataRegistry().getPluginDataSerializer().serialize(data));
                if (send.isSuccess()) {
                    return data;
                } else {
                    throw new CompletionException(new Throwable("Failed to send data to the proxy."));
                }
            });
    }

    @Override
    public <T extends PluginData> CompletableFuture<T> asyncLoadData(PluginDataIdentifier<T> identifier) {
        return CompletableFuture.supplyAsync(() -> loadData(identifier.classType(), identifier.uuid())).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while asynchronously loading plugin data from the database: " + e.getMessage());
            throw new CompletionException(new Throwable("Failed to asynchronously load data for" + identifier));
        });
    }

    @Override
    public <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> asyncDeleteDataAndSync(PluginDataIdentifier<T> identifier) {
        return CompletableFuture.supplyAsync(() -> deleteData(identifier.classType(), identifier.uuid())).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while asynchronously deleting data from the database: " + e.getMessage());
            throw new CompletionException(new Throwable("Failed to asynchronously delete data and sync it with other servers for" + identifier));
        }).thenApplyAsync(id -> {
            Sender send = crescentCore.getCrescentSunConnection().send(crescentCore.getPluginDataRegistry().getPluginDataSerializer().serialize(identifier));
            if (send.isSuccess()) {
                return id;
            } else {
                throw new CompletionException(new Throwable("Failed to send data to the proxy upon deletion."));
            }
        });
    }

    public  <T extends PluginData> void addDataTypeToCache(Class<T> classType) {
        primaryKeyCache.put(classType, findPrimaryKeyField(classType));
        serializableFieldsCache.put(classType, findSerializableFields(classType));
        String tableName = findTableName(classType);
        tableNameFromClassCache.put(classType, tableName);
        classFromTableNameCache.put(tableName, classType);
    }

    @Override
    public <T extends PluginData> T getData(PluginDataIdentifier<T> pluginDataIdentifier) {
        PluginData data = repository.get(pluginDataIdentifier);
        if (data == null) {
            return null;
        }
        Class<T> dataClass = pluginDataIdentifier.classType();
        if (dataClass.isInstance(data)) {
            return dataClass.cast(data);
        } else {
            // Handle the type mismatch
            throw new ClassCastException("Data is not of type " + dataClass.getName() + " but is of type " + data.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<PluginDataIdentifier<?>, PluginData> getAllData() {
        return ImmutableMap.copyOf(repository);
    }

    @Override
    public <T extends PluginData> Map<UUID, T> getAllDataOfType(Class<T> dataClass) {
        Map<UUID, T> result = new HashMap<>();
        for (PluginDataIdentifier<? extends PluginData> identifier : repository.keySet()) {
            if (identifier.classType().equals(dataClass)) {
                result.put(identifier.uuid(), dataClass.cast(repository.get(identifier)));
            }
        }
        return result;
    }

    @Override
    public void insertData(UUID uuid, PluginData pluginData, boolean shouldReplace) {
        Class<? extends PluginData> dataClass = pluginData.getClass();
        if (primaryKeyCache.get(dataClass) == null) {
            throw new ClassNotRegisteredException("Couldn't insert data instance: Class not registered: " + dataClass.getName());
        }

        PluginDataIdentifier<? extends PluginData> identifier = new PluginDataIdentifier<>(dataClass, uuid);
        if (getData(identifier) != null && !shouldReplace) {
            crescentCore.getLogger().warning("Data instance already exists for identifier " + identifier + ", yet the new one was marked as not to replace. Ignoring.");
        } else {
            repository.put(identifier, pluginData);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PluginData> PluginDataIdentifier<T> removeData(PluginDataIdentifier<T> identifier) {
        Class<T> dataClass = identifier.classType();
        if (primaryKeyCache.get(dataClass) == null) {
            throw new ClassNotRegisteredException("Error removing data instance: Class not registered: " + dataClass.getName());
        }
        PluginData dataInstance = repository.remove(identifier);
        if (dataInstance == null) {
            crescentCore.getLogger().warning("No data instance found for identifier" + identifier);
            return null;
        } else {
            return identifier;
        }
    }

    public <T extends PluginData> T saveData(@NotNull T pluginData) {
        long startTime = System.currentTimeMillis();
        Class<? extends PluginData> dataClass = pluginData.getClass();
        String fullTableName = getTableNameFromPluginDataClass(dataClass);
        String queryTemplate = INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.get(fullTableName);

        if (queryTemplate == null) {
            crescentCore.getLogger().warning("No save query template found for table " + fullTableName);
            return null;
        }
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Prevent concurrent modifications

            int index = 1;
            for (Field field : getSerializableFields(dataClass).keySet()) {
                if (field.equals(getPrimaryKeyField(dataClass).key())) {
                    if (!(field.get(pluginData) instanceof UUID)) {
                        crescentCore.getLogger().severe("Error while saving plugin data: primary key field is not of type UUID for table " + fullTableName);
                        throw new IllegalStateException("Primary key field is not of type UUID for table " + fullTableName);
                    }
                }
                statement.setObject(index++, field.get(pluginData).toString());
            }

            statement.executeUpdate();
            crescentCore.getLogger().info("Successfully saved plugin data to the database for table " + fullTableName + " in " + (System.currentTimeMillis() - startTime) + "ms");
            return pluginData;
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while saving plugin data to the database: " + e.getMessage());
        } catch (IllegalAccessException e) {
            crescentCore.getLogger().severe("An error occurred while accessing plugin data fields: " + e.getMessage());
        }
        throw new IllegalStateException("Failed to save plugin data to the database.");
    }

    @ApiStatus.Internal
    public <T extends PluginData> T loadData(Class<T> dataClass, UUID uuid) {
        long startTime = System.currentTimeMillis();
        String fullTableName = getTableNameFromPluginDataClass(dataClass);
        Pair<Field, DatabaseColumn> primaryKeyPair = getPrimaryKeyField(dataClass);
        if (primaryKeyPair == null) {
            crescentCore.getLogger().warning("No primary key found for table " + fullTableName);
            return null;
        }
        String queryTemplate = SELECT_PLUGIN_DATA_QUERIES.get(fullTableName);
        if (queryTemplate == null) {
            crescentCore.getLogger().warning("No load query template found for table " + fullTableName);
            return null;
        }
        queryTemplate += " WHERE " + primaryKeyPair.value().columnName() + " = ?";
        PluginData dataInstance = null;
        try (Connection connection = dbManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
            statement.setObject(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                Constructor<? extends PluginData> constructor = dataClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                dataInstance = constructor.newInstance();
                if (resultSet.next()) {
                    int index = 1;
                    for (Field field : getSerializableFields(dataClass).keySet()) {
                        Object value;
                        if (field.getType().equals(UUID.class)) {
                            // Retrieve the UUID as a string and convert it to a UUID
                            value = UUID.fromString(resultSet.getString(index++));
                        } else {
                            // Retrieve the value normally for other types
                            value = resultSet.getObject(index++, field.getType());
                        }
                        field.set(dataInstance, value); // Set the value to the field
                    }
                    // Add data instance to repository
                    insertData(uuid, dataInstance, true);
                }
            } catch (InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                crescentCore.getLogger().severe("An error occurred while trying to reflectively instantiate the plugin data class (On Load): " + e.getMessage());
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new IllegalStateException("Failed to instantiate plugin data class " + dataClass.getName() + ". Make sure it has a public no-args constructor.");
            }
            crescentCore.getLogger().info("Successfully loaded plugin data from the database for table " + fullTableName + " in " + (System.currentTimeMillis() - startTime) + "ms");
            return dataClass.cast(dataInstance);
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while loading plugin data from the database: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to load plugin data from the database.");
        }
    }

    @ApiStatus.Internal
    public <T extends PluginData> PluginDataIdentifier<T> deleteData(Class<T> dataClass, UUID uuid) {
        String tableName = getTableNameFromPluginDataClass(dataClass);
        Pair<Field, DatabaseColumn> primaryKeyPair = getPrimaryKeyField(dataClass);

        if (primaryKeyPair == null) {
            crescentCore.getLogger().warning("No primary key found for table " + tableName);
            return null;
        }

        String queryTemplate = "DELETE FROM " + tableName + " WHERE " + primaryKeyPair.value().columnName() + " = ?";

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTemplate)) {

            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Prevent concurrent modifications
            statement.setObject(1, uuid.toString());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                crescentCore.getLogger().info("Successfully deleted data with primary key " + uuid + " from table " + tableName);
                return new PluginDataIdentifier<>(dataClass, uuid);
            } else {
                crescentCore.getLogger().warning("No data found with primary key " + uuid + " in table " + tableName);
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while deleting data from the database: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @ApiStatus.Internal
    public boolean saveAllData() {
        try (Connection connection = dbManager.getConnection()) { // Connects to DB
            connection.setAutoCommit(false); //Disables autocommit
            for (String fullTableName : dbManager.getPluginTableNames()) { //Loops through all tables
                String queryTemplate = INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.get(fullTableName); // Gets the query for the current table
                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) { // Prepares the statement
                    Class<? extends PluginData> dataClass = getPluginDataClassFromRawTableName(fullTableName); //Gets the raw class.java which is instantiated to store data
                    for (UUID uuid : getAllDataOfType(dataClass).keySet()) { // Loops over all the keys used to represent each instance of the raw class.
                        PluginData dataInstance = getData(dataClass, uuid); // Gets the specific instance
                        if (dataInstance == null) {
                            crescentCore.getLogger().warning("Data instance is null for table " + fullTableName + " with UUID " + uuid);
                            continue;
                        }
                        int index = 1; // Index used to set the values in the prepared statement
                        for (Field field : dataInstance.getClass().getDeclaredFields()) { // Loops over each field in the instance
                            field.setAccessible(true);
                            Object value = field.get(dataInstance); // Gets the value of the field, using the instance
                            if (value instanceof UUID) {
                                value = value.toString();
                            }
                            statement.setObject(index++, value); // Sets the value in the prepared statement
                        }
                        statement.executeUpdate();
                    }
                }
            }
            connection.commit();
            return true;
        } catch (Exception e) {
            crescentCore.getLogger().severe("An error occurred while saving all plugin data to the database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean loadAllData() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dbManager.getConnection()) {
            for (String fullTableName : dbManager.getPluginTableNames()) {
                String queryTemplate = SELECT_PLUGIN_DATA_QUERIES.get(fullTableName);
                Class<? extends PluginData> dataClass = getPluginDataClassFromRawTableName(fullTableName);

                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            // Create a new instance of the PluginData class
                            Constructor<? extends PluginData> constructor = dataClass.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            PluginData dataInstance = constructor.newInstance();

                            int index = 1; // Index used to retrieve the values from the result set
                            UUID uuid = null; // The key used to store the instance in the repository

                            Map<Field, DatabaseColumn> serializableFields = getSerializableFields(dataClass);
                            for (Field field : serializableFields.keySet()) {
                                DatabaseColumn columnAnnotation = serializableFields.get(field);
                                Object value;
                                if (field.getType().equals(UUID.class)) {
                                    // Retrieve the UUID as a string and convert it to a UUID
                                    value = UUID.fromString(resultSet.getString(index++));
                                } else {
                                    // Retrieve the value normally for other types
                                    value = resultSet.getObject(index++, field.getType());
                                }
                                field.set(dataInstance, value); // Set the value to the field

                                // If this field is the primary key, store its value as the key for the instance
                                if (columnAnnotation != null && columnAnnotation.isPrimaryKey() && value instanceof UUID) {
                                    uuid = (UUID) value;
                                }
                            }

                            // Ensure the key is not null before adding it to the repository
                            if (uuid != null) {
                                insertData(uuid, dataInstance, true);
                            } else {
                                throw new IllegalStateException("Primary key is null for table " + fullTableName);
                            }
                        }
                    }
                }
            }
            crescentCore.getLogger().info("Successfully loaded all plugin data from the database in " + (System.currentTimeMillis() - startTime) + "ms");
            return true;
        } catch (Exception e) {
            crescentCore.getLogger().severe("An error occurred while loading all plugin data from the database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean reloadAllData() {
        long startTime = System.currentTimeMillis();
        clearAllData();
        boolean dataLoaded = loadAllData();
        for (PluginData dataInstance : getAllData().values()) {
            dataInstance.tryInit();
        }
        crescentCore.getLogger().info("Successfully reloaded all plugin data in " + (System.currentTimeMillis() - startTime) + "ms");
        return dataLoaded;
    }

    @ApiStatus.Internal
    public CompletableFuture<Boolean> asyncLoadAllData() {
        return CompletableFuture.supplyAsync(this::loadAllData).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while loading all plugin data async from the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        });
    }

    private void populateInsertOrUpdatePluginDataQueries() {
        for (String fullTableName : dbManager.getPluginTableNames()) {
            Class<? extends PluginData> dataClass = getPluginDataClassFromRawTableName(fullTableName);

            StringBuilder columnsPart = new StringBuilder();
            StringBuilder valuesPart = new StringBuilder();
            StringBuilder updatePart = new StringBuilder();

            String baseQuery = "INSERT INTO " + fullTableName + " (";
            String updateQuery = " ON DUPLICATE KEY UPDATE ";

            Map<Field, DatabaseColumn> serializableFields = getSerializableFields(dataClass);
            for (Field field : serializableFields.keySet()) {
                DatabaseColumn columnAnnotation = serializableFields.get(field);
                String columnName = columnAnnotation.columnName();
                columnsPart.append(columnName).append(", ");
                valuesPart.append("?").append(", ");

                // Include this field in the update part only if it is not a primary key
                if (!columnAnnotation.isPrimaryKey()) {
                    updatePart.append(columnName).append(" = VALUES(").append(columnName).append("), ");
                }
            }

            if (!columnsPart.isEmpty()) {
                columnsPart.setLength(columnsPart.length() - 2); // Remove trailing comma
                valuesPart.setLength(valuesPart.length() - 2);   // Remove trailing comma
                if (!updatePart.isEmpty()) {
                    updatePart.setLength(updatePart.length() - 2); // Remove trailing comma
                }
            }

            String finalQuery = baseQuery + columnsPart + ") VALUES (" + valuesPart + ")";
            if (!updatePart.isEmpty()) {
                finalQuery += updateQuery + updatePart;
            }

            INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.put(fullTableName, finalQuery);
        }
    }

    private void populateSelectPluginDataQueries() {
        for (String fullTableName : crescentCore.getDatabaseManager().getPluginTableNames()) {
            Class<? extends PluginData> dataClass = getPluginDataClassFromRawTableName(fullTableName);
            boolean hasPrimaryKey = false;

            Map<Field, DatabaseColumn> serializableFields = getSerializableFields(dataClass);
            for (Field field : serializableFields.keySet()) {

                DatabaseColumn columnAnnotation = serializableFields.get(field);
                if (columnAnnotation.isPrimaryKey()) {
                    hasPrimaryKey = true;
                    String query = "SELECT * FROM " + fullTableName;
                    SELECT_PLUGIN_DATA_QUERIES.put(fullTableName, query);
                    break; // Primary key found, no need to check further fields
                }
            }

            if (!hasPrimaryKey) {
                crescentCore.getLogger().warning("No primary key found for table " + fullTableName);
            }
        }
    }

    /**
     * Retrieves the table name for the given plugin data class.
     * The table name is constructed using the plugin's simple name and the table name specified in the DatabaseTable annotation.
     *
     * @param dataClass The class of the plugin data.
     * @return The table name in the format "pluginName_tableName", or null if the class is not annotated with DatabaseTable.
     */
    public String getTableNameFromPluginDataClass(Class<? extends PluginData> dataClass) {
        return TableNameUtil.appendPluginDataTablePrefix(dataClass);
    }

    /**
     * Retrieves the plugin data class corresponding to the given full table name.
     * The full table name is expected to be in the format "pluginName_tableName".
     *
     * @param tableName The full table name in the format "pluginName_tableName".
     * @return The class of the plugin data that matches the given full table name, or null if no match is found.
     */
    public Class<? extends PluginData> getPluginDataClassFromRawTableName(String tableName) {
        return classFromTableNameCache.get(tableName.toLowerCase());
    }

    /**
     * Retrieves the primary key field and its corresponding DatabaseColumn annotation from the given plugin data class.
     *
     * @param dataClass The class of the plugin data.
     * @return A Pair containing the primary key field and its DatabaseColumn annotation, or null if no primary key is found.
     */
    public Pair<Field, DatabaseColumn> getPrimaryKeyField(Class<? extends PluginData> dataClass) {
        return primaryKeyCache.get(dataClass);
    }

    @NotNull
    public Map<Field, DatabaseColumn> getSerializableFields(Class<? extends PluginData> dataClass) {
        return serializableFieldsCache.get(dataClass);
    }

    private Pair<Field, DatabaseColumn> findPrimaryKeyField(Class<? extends PluginData> dataClass) {
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

    private Map<Field, DatabaseColumn> findSerializableFields(Class<? extends PluginData> dataClass) {
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


    private String findTableName(Class<? extends PluginData> dataClass) {
        if (!dataClass.isAnnotationPresent(DatabaseTable.class)) {
            throw new IllegalStateException("Plugin data class " + dataClass.getName() + " does not have a DatabaseTable annotation!");
        }
        DatabaseTable annotation = dataClass.getAnnotation(DatabaseTable.class);
        String fullTableName = annotation.plugin().getSimpleName() + "_" + annotation.tableName();
        return fullTableName.toLowerCase();
    }

    public void clearDataOfType(Class<? extends PluginData> classType) {
        repository.entrySet().removeIf(entry -> entry.getKey().classType().equals(classType));
    }

    @ApiStatus.Internal
    public void clearAllData() {
        repository.clear();
    }

}
