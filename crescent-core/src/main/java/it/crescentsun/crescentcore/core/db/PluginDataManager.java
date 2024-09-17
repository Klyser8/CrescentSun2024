package it.crescentsun.crescentcore.core.db;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static it.crescentsun.crescentcore.CrescentCore.PLUGIN_DATA_REGISTRY;

public class PluginDataManager {
    private final Map<String, String> INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES = new HashMap<>();
    private final Map<String, String> SELECT_PLUGIN_DATA_QUERIES = new HashMap<>();
    private final CrescentCore crescentCore;
    private final DatabaseManager dbManager;

    public PluginDataManager(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        dbManager = crescentCore.getDatabaseManager();
        populateInsertOrUpdatePluginDataQueries();
        populateSelectPluginDataQueries();
    }

    public CompletableFuture<PluginData> asyncSaveData(@NotNull PluginData pluginData) {
        return CompletableFuture.supplyAsync(() -> saveData(pluginData)) // Start the async task
                .exceptionally(e -> {
                    crescentCore.getLogger().severe("An error occurred while asynchronously saving plugin data to the database: " + e.getMessage());
                    e.printStackTrace(); // Log the full stack trace for better debugging
                    return null;
                });
    }

    public CompletableFuture<Boolean> asyncLoadData(Class<? extends PluginData> dataClass, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> loadData(dataClass, uuid)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while asynchronously loading plugin data from the database: " + e.getMessage());
            return false;
        });
    }

    public CompletableFuture<Boolean> asyncDeleteData(Class<? extends PluginData> dataClass, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> deleteData(dataClass, uuid)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while asynchronously deleting data from the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        });
    }

    @ApiStatus.Internal
    public PluginData saveData(@NotNull PluginData dataInstance) {
        Class<? extends PluginData> dataClass = dataInstance.getClass();
        String fullTableName = PluginDataRepository.getTableNameFromPluginDataClass(dataClass);
        String queryTemplate = INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.get(fullTableName);

        if (queryTemplate == null) {
            crescentCore.getLogger().warning("No save query template found for table " + fullTableName);
            return null;
        }
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Prevent concurrent modifications

            int index = 1;
            for (Field field : PluginDataRepository.getSerializableFields(dataClass).keySet()) {
                if (field.equals(PluginDataRepository.getPrimaryKeyField(dataClass).key())) {
                    if (!(field.get(dataInstance) instanceof UUID)) {
                        crescentCore.getLogger().severe("Error while saving plugin data: primary key field is not of type UUID for table " + fullTableName);
                        return null;
                    }
                }
                statement.setObject(index++, field.get(dataInstance).toString());
            }

            statement.executeUpdate();
            return dataInstance;
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while saving plugin data to the database: " + e.getMessage());
        } catch (IllegalAccessException e) {
            crescentCore.getLogger().severe("An error occurred while accessing plugin data fields: " + e.getMessage());
        }
        return null;
    }

    @ApiStatus.Internal
    public boolean loadData(Class<? extends PluginData> dataClass, UUID uuid) {
        String fullTableName = PluginDataRepository.getTableNameFromPluginDataClass(dataClass);
        Pair<Field, DatabaseColumn> primaryKeyPair = PluginDataRepository.getPrimaryKeyField(dataClass);
        if (primaryKeyPair == null) {
            crescentCore.getLogger().warning("No primary key found for table " + fullTableName);
            return false;
        }
        String queryTemplate = SELECT_PLUGIN_DATA_QUERIES.get(fullTableName);
        if (queryTemplate == null) {
            crescentCore.getLogger().warning("No load query template found for table " + fullTableName);
            return false;
        }
        queryTemplate += " WHERE " + primaryKeyPair.value().columnName() + " = ?";
        try (Connection connection = dbManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
            statement.setObject(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Constructor<? extends PluginData> constructor = dataClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    PluginData dataInstance = constructor.newInstance();
                    int index = 1;
                    for (Field field : PluginDataRepository.getSerializableFields(dataClass).keySet()) {
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
                    crescentCore.getPluginDataRepository().addDataInstance(dataClass, uuid, dataInstance);
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                crescentCore.getLogger().severe("An error occurred while trying to reflectively instantiate the plugin data class (On Load): " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while loading plugin data from the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @ApiStatus.Internal
    public boolean deleteData(Class<? extends PluginData> dataClass, UUID uuid) {
        String tableName = PluginDataRepository.getTableNameFromPluginDataClass(dataClass);
        Pair<Field, DatabaseColumn> primaryKeyPair = PluginDataRepository.getPrimaryKeyField(dataClass);

        if (primaryKeyPair == null) {
            crescentCore.getLogger().warning("No primary key found for table " + tableName);
            return false;
        }

        String queryTemplate = "DELETE FROM " + tableName + " WHERE " + primaryKeyPair.value().columnName() + " = ?";

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTemplate)) {

            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Prevent concurrent modifications
            statement.setObject(1, uuid.toString());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                crescentCore.getLogger().info("Successfully deleted data with primary key " + uuid + " from table " + tableName);
                return true;
            } else {
                crescentCore.getLogger().warning("No data found with primary key " + uuid + " in table " + tableName);
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while deleting data from the database: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @ApiStatus.Internal
    public boolean saveAllData() {
        try (Connection connection = dbManager.getConnection()) { // Connects to DB
            connection.setAutoCommit(false); //Disables autocommit
            for (String fullTableName : dbManager.getPluginTableNames()) { //Loops through all tables
                String queryTemplate = INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.get(fullTableName); // Gets the query for the current table
                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) { // Prepares the statement
                    Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName); //Gets the raw class.java which is instantiated to store data
                    for (UUID uuid : PLUGIN_DATA_REGISTRY.getDataRepository().getAllDataOfType(dataClass).keySet()) { // Loops over all the keys used to represent each instance of the raw class.
                        PluginData dataInstance = PLUGIN_DATA_REGISTRY.getDataRepository().getData(dataClass, uuid); // Gets the specific instance
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
        try (Connection connection = dbManager.getConnection()) {
            for (String fullTableName : dbManager.getPluginTableNames()) {
                String queryTemplate = SELECT_PLUGIN_DATA_QUERIES.get(fullTableName);
                Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName);

                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            // Create a new instance of the PluginData class
                            Constructor<? extends PluginData> constructor = dataClass.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            PluginData dataInstance = constructor.newInstance();

                            int index = 1; // Index used to retrieve the values from the result set
                            UUID uuid = null; // The key used to store the instance in the repository

                            Map<Field, DatabaseColumn> serializableFields = PluginDataRepository.getSerializableFields(dataClass);
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
                                PLUGIN_DATA_REGISTRY.getDataRepository().addDataInstance(dataClass, uuid, dataInstance);
                            } else {
                                throw new IllegalStateException("Primary key is null for table " + fullTableName);
                            }
                        }
                    }
                }
            }
            crescentCore.getLogger().info("Successfully loaded all plugin data from the database.");
            return true;
        } catch (Exception e) {
            crescentCore.getLogger().severe("An error occurred while loading all plugin data from the database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean reloadAllData() {
        PLUGIN_DATA_REGISTRY.getDataRepository().clearAllData();
        return loadAllData();
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
            Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName);

            StringBuilder columnsPart = new StringBuilder();
            StringBuilder valuesPart = new StringBuilder();
            StringBuilder updatePart = new StringBuilder();

            String baseQuery = "INSERT INTO " + fullTableName + " (";
            String updateQuery = " ON DUPLICATE KEY UPDATE ";

            Map<Field, DatabaseColumn> serializableFields = PluginDataRepository.getSerializableFields(dataClass);
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
            Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName);
            boolean hasPrimaryKey = false;

            Map<Field, DatabaseColumn> serializableFields = PluginDataRepository.getSerializableFields(dataClass);
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

}
