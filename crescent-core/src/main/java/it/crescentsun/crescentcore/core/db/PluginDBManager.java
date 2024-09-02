package it.crescentsun.crescentcore.core.db;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static it.crescentsun.crescentcore.CrescentCore.PLUGIN_DATA_REGISTRY;

public class PluginDBManager {
    private final Map<String, String> INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES = new HashMap<>();
    private final Map<String, String> SELECT_PLUGIN_DATA_QUERIES = new HashMap<>();
    private final CrescentCore crescentCore;
    private final DatabaseManager dbManager;

    public PluginDBManager(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        dbManager = crescentCore.getDatabaseManager();
        populateInsertOrUpdatePluginDataQueries();
        populateSelectPluginDataQueries();
    }

    private void populateInsertOrUpdatePluginDataQueries() {
        for (String fullTableName : dbManager.getPluginTableNames()) {
            Class<? extends PluginData> dataClass = dbManager.getPluginDataClassFromFullTableName(fullTableName);

            StringBuilder columnsPart = new StringBuilder();
            StringBuilder valuesPart = new StringBuilder();
            StringBuilder updatePart = new StringBuilder();

            String baseQuery = "INSERT INTO " + fullTableName + " (";
            String updateQuery = " ON DUPLICATE KEY UPDATE ";

            for (Field field : dataClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(DatabaseColumn.class)) {
                    continue;
                }
                DatabaseColumn columnAnnotation = field.getAnnotation(DatabaseColumn.class);
                String columnName = columnAnnotation.columnName();
                columnsPart.append(columnName).append(", ");
                valuesPart.append("?").append(", ");

                updatePart.append(columnName).append(" = VALUES(").append(columnName).append("), ");
            }
            if (!columnsPart.isEmpty()) {
                columnsPart.setLength(columnsPart.length() - 2);
                valuesPart.setLength(valuesPart.length() - 2);
                updatePart.setLength(updatePart.length() - 2);
            }

            String finalQuery = baseQuery + columnsPart + ") VALUES (" + valuesPart + ")" + updateQuery + updatePart;
            INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.put(fullTableName, finalQuery);
        }
    }

    private void populateSelectPluginDataQueries() {
        for (String fullTableName : crescentCore.getDatabaseManager().getPluginTableNames()) {
            Class<? extends PluginData> dataClass = crescentCore.getDatabaseManager().getPluginDataClassFromFullTableName(fullTableName);
            boolean hasPrimaryKey = false;

            for (Field field : dataClass.getDeclaredFields()) {
                field.setAccessible(true);

                if (!field.isAnnotationPresent(DatabaseColumn.class)) {
                    continue;
                }

                DatabaseColumn columnAnnotation = field.getAnnotation(DatabaseColumn.class);
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

    public CompletableFuture<Boolean> asyncSaveAllData() {
        return CompletableFuture.supplyAsync(this::saveAllData).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while saving plugin data to the database: " + e.getMessage());
            return false;
        });
    }

    public boolean saveAllData() {
        try (Connection connection = dbManager.getConnection()) { // Connects to DB
            connection.setAutoCommit(false); //Disables autocommit
            for (String fullTableName : dbManager.getPluginTableNames()) { //Loops through all tables
                String queryTemplate = INSERT_OR_UPDATE_PLUGIN_DATA_QUERIES.get(fullTableName); // Gets the query for the current table
                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) { // Prepares the statement
                    Class<? extends PluginData> dataClass = dbManager.getPluginDataClassFromFullTableName(fullTableName); //Gets the raw class.java which is instantiated to store data
                    for (Object dataKey : PLUGIN_DATA_REGISTRY.getDataRepository().getAllData(dataClass).keySet()) { // Loops over all the keys used to represent each instance of the raw class.
                        PluginData dataInstance = PLUGIN_DATA_REGISTRY.getDataRepository().getData(dataClass, dataKey); // Gets the specific instance
                        int index = 1; // Index used to set the values in the prepared statement
                        for (Field field : dataInstance.getClass().getDeclaredFields()) { // Loops over each field in the instance
                            field.setAccessible(true);
                            Object value = field.get(dataInstance); // Gets the value of the field, using the instance
                            if (value instanceof UUID uuid) {
                                value = uuid.toString();
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
            crescentCore.getLogger().severe("An error occurred while saving plugin data to the database: " + e.getMessage());
        }
        return false;
    }

    public boolean loadAllData() {
        try (Connection connection = dbManager.getConnection()) {
            for (String fullTableName : dbManager.getPluginTableNames()) {
                String queryTemplate = SELECT_PLUGIN_DATA_QUERIES.get(fullTableName);
                System.out.println("Query: " + queryTemplate);
                Class<? extends PluginData> dataClass = dbManager.getPluginDataClassFromFullTableName(fullTableName);

                try (PreparedStatement statement = connection.prepareStatement(queryTemplate)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            System.out.println("Result set: " + resultSet);
                            // Create a new instance of the PluginData class
                            PluginData dataInstance = dataClass.getDeclaredConstructor().newInstance();

                            int index = 1; // Index used to retrieve the values from the result set
                            Object key = null; // The key used to store the instance in the repository

                            for (Field field : dataClass.getDeclaredFields()) {
                                field.setAccessible(true);
                                DatabaseColumn columnAnnotation = field.getAnnotation(DatabaseColumn.class);
                                Object value;
                                if (field.getType().equals(UUID.class)) {
                                    // Retrieve the UUID as a string and convert it to a UUID
                                    String uuidString = resultSet.getString(index++);
                                    value = UUID.fromString(uuidString);
                                } else {
                                    // Retrieve the value normally for other types
                                    value = resultSet.getObject(index++, field.getType());
                                }
                                field.set(dataInstance, value); // Set the value to the field

                                // If this field is the primary key, store its value as the key for the instance
                                if (columnAnnotation != null && columnAnnotation.isPrimaryKey()) {
                                    key = value;
                                }
                            }

                            // Ensure the key is not null before adding it to the repository
                            if (key != null) {
                                PLUGIN_DATA_REGISTRY.getDataRepository().addData(dataClass, key, dataInstance);
                            } else {
                                throw new IllegalStateException("Primary key is null for table " + fullTableName);
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            crescentCore.getLogger().severe("An error occurred while loading plugin data from the database: " + e.getMessage());
        }
        return false;
    }

    public CompletableFuture<Boolean> asyncLoadAllData() {
        return CompletableFuture.supplyAsync(this::loadAllData).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while loading plugin data from the database: " + e.getMessage());
            return false;
        });
    }

}
