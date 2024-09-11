package it.crescentsun.crescentcore.core.db;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.DataEntry;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static it.crescentsun.crescentcore.CrescentCore.PLAYER_DATA_ENTRY_REGISTRY;
import static it.crescentsun.crescentcore.CrescentCore.PLUGIN_DATA_REGISTRY;

public class DatabaseManager {

    private final HikariDataSource dataSource;
    private PlayerDBManager playerDBManager = null;
    private PluginDBManager pluginDBManager = null;
    private final CrescentCore crescentCore;
    private final List<String> tableNames = new ArrayList<>();
    public DatabaseManager(CrescentCore crescentCore, String host, int port, String database, String username, String password, int maxPoolSize) {
        this.crescentCore = crescentCore;

        // Configure and initialize the HikariDataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(maxPoolSize); // Adjust the pool size as needed

        dataSource = new HikariDataSource(config);
    }

    public void initTables() {
        // Populate tableNames for each plugin that has registered player data
        for (NamespacedKey namespacedKey : PLAYER_DATA_ENTRY_REGISTRY.clonePlayerDataEntryRegistry().keySet()) {
            String tableName = namespacedKey.namespace();
            if (!tableNames.contains(tableName)) {
                tableNames.add(tableName); //<pluginname>_player_data
            }
        }
        // Populate tableNames for each plugin that has registered plugin data
        for (Class<? extends PluginData> clazz : PLUGIN_DATA_REGISTRY.getRegistry()) {
            String fullTableName = PluginDataRepository.getTableNameFromPluginDataClass(clazz);
            if (!tableNames.contains(fullTableName)) {
                tableNames.add(fullTableName); //<pluginname>_<plugindata>
            }
        }
        createTables();
        updateAllTables();
    }

    /**
     * Creates the plugin tables for each plugin that has registered additional data.
     * The table name will be the name of the plugin.
     */
    private void createTables() {
        try (Statement statement = getConnection().createStatement()) {
            for (String name : tableNames) {
                String query = populateCreateTableQuery(name);
                if (statement.executeUpdate(query) > 0) {
                    crescentCore.getLogger().info("Created " + name + " table");
                }
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error creating tables: " + e.getMessage());
        }
    }

    /**
     * Creates the query to create a table for a plugin.
     * The table name will be the name of the plugin.
     *
     * @param tableName The name of the table to create
     * @return The query to create the table
     */
    private String populateCreateTableQuery(String tableName) {
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (%COLUMN_LIST%);";
        StringBuilder columns = new StringBuilder();
        boolean isPlayerData = tableName.endsWith("_player_data");

        if (isPlayerData) {
            addPlayerPrimaryKeyAndColumns(tableName, columns);
        } else {
            addPluginPrimaryKeyAndColumns(tableName, columns);
        }

        query = query.replace("%COLUMN_LIST%", columns.toString().trim().replaceAll(",$", ""));
        return query;
    }

    private void addPlayerPrimaryKeyAndColumns(String tableName, StringBuilder columns) {
        columns.append(CrescentNamespaceKeys.PLAYER_UUID.getKey()).append(" VARCHAR(36) NOT NULL PRIMARY KEY, ");
        Map<NamespacedKey, DataEntry<?>> playerDataRegistry = PLAYER_DATA_ENTRY_REGISTRY.getPlayerDataEntryForNamespace(tableName);
        for (NamespacedKey namespacedKey : playerDataRegistry.keySet()) {
            DataType dataType = playerDataRegistry.get(namespacedKey).getType();
            String columnName = namespacedKey.getKey();
            if (dataType == null) {
                crescentCore.getLogger().warning("Could not determine data type for " + columnName + " in " + tableName + " table");
                continue;
            }
            columns.append(columnName).append(" ").append(dataType.getSqlType()).append(", ");
        }
    }

    private void addPluginPrimaryKeyAndColumns(String fullTableName, StringBuilder columns) {
        Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName);
        if (dataClass == null) {
            crescentCore.getLogger().warning("Could not find plugin data class for " + fullTableName + " table");
            return;
        }
        Map<Field, DatabaseColumn> serializableFields = PluginDataRepository.getSerializableFields(dataClass);
        for (Field field : serializableFields.keySet()) {
            DatabaseColumn annotation = serializableFields.get(field);
            if (annotation.isPrimaryKey()) {
                if (annotation.dataType().equals(DataType.VARCHAR_36) && annotation.columnName().equals("uuid")) {
                    columns.append(annotation.columnName()).append(" ").append(annotation.dataType().getSqlType()).append(" PRIMARY KEY, ");
                } else {
                    crescentCore.getLogger().severe("Primary key for " + fullTableName + " table must be a VARCHAR(36) column named 'uuid'");
                }
            } else {
                columns.append(annotation.columnName()).append(" ").append(annotation.dataType().getSqlType()).append(", ");
            }
        }
    }

    private void updateAllTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String name : tableNames) {
                if (name.endsWith("_player_data")) {
                    updatePlayerDataTable(statement, name);
                } else {
                    updatePluginDataTable(statement, name);
                }
            }

        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error updating tables: " + e.getMessage());
        }
    }

    private void updatePlayerDataTable(Statement statement, String tableName) throws SQLException {
        String[] splitName = tableName.split("_", 2);
        String pluginName = splitName[0];
        // Gets all player data tied to a plugin
        Map<NamespacedKey, DataEntry<?>> playerDataEntryForNamespace = PLAYER_DATA_ENTRY_REGISTRY.getPlayerDataEntryForNamespace(pluginName);
        // Iterate over each additional data entry
        for (NamespacedKey namespacedKey : playerDataEntryForNamespace.keySet()) {
            String columnName = namespacedKey.getKey();
            // Check if the column exists
            if (doesColumnExist(tableName, columnName)) {
                continue;
            }
            // Determine the SQL data type for the column based on the value's type
            DataType dataType = playerDataEntryForNamespace.get(namespacedKey).getType();
            addColumn(tableName, columnName, dataType, false, statement);
        }
    }

    private void updatePluginDataTable(Statement statement, String fullTableName) throws SQLException {
        Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(fullTableName);
        if (dataClass == null) {
            crescentCore.getLogger().warning("Could not find plugin data class for " + fullTableName + " table");
            return;
        }
        Map<Field, DatabaseColumn> serializableFields = PluginDataRepository.getSerializableFields(dataClass);
        for (Field field : serializableFields.keySet()) {
            DatabaseColumn annotation = serializableFields.get(field);
            if (doesColumnExist(fullTableName, annotation.columnName())) {
                continue;
            }
            boolean isPrimaryKey = annotation.isPrimaryKey();;
            addColumn(fullTableName, annotation.columnName(), annotation.dataType(), isPrimaryKey, statement);
        }
    }

    private final Map<String, Set<String>> columnCache = new HashMap<>();

    private boolean doesColumnExist(String tableName, String columnName) {
        Set<String> columnNames = columnCache.computeIfAbsent(tableName.toLowerCase(), this::fetchColumnNames);
        return columnNames.contains(columnName.toLowerCase());
    }

    private Set<String> fetchColumnNames(String tableName) {
        Set<String> columnNames = new HashSet<>();
        try (Connection connection = getConnection();
             ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                columnNames.add(columnName.toLowerCase()); // To handle case-insensitive checks
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error fetching columns for table " + tableName + ": " + e.getMessage());
        }
        return columnNames;
    }


    /*private boolean doesColumnExist(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData dbMetaData = connection.getMetaData();
            try (ResultSet rs = dbMetaData.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
//                    crescentCore.getLogger().info("Column " + columnName + " exists in " + tableName + " table");
                    return true;
                } else {
//                    crescentCore.getLogger().info("Column " + columnName + " does not exist in " + tableName + " table");
                    return false;
                }
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error checking column existence: " + e.getMessage());
        }
        return false;
    }*/

    private void addColumn(String tableName, String columnName, DataType dataType, boolean isPrimaryKey, Statement statement) throws SQLException {
        if (dataType == null) {
            crescentCore.getLogger().warning("Could not determine data type for " + columnName + " in " + tableName + " table");
            return;
        }
        String query;
        if (isPrimaryKey) {
            query = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType.getSqlType() + " PRIMARY KEY;";
        } else {
            query = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType.getSqlType() + ";";
        }

        // Execute the query to add the new column
        statement.executeUpdate(query);
        crescentCore.getLogger().info("Added column " + columnName + " to " + tableName + " table");
    }

    public void saveEverything() {
        getPlayerDataManager().saveAllData();
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error getting connection: " + e.getMessage());
            return null;
        }
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void initPlayerDataManager() {
        if (playerDBManager == null) {
            playerDBManager = new PlayerDBManager(crescentCore, this);
        }
    }

    public void initPluginDataManager() {
        if (pluginDBManager == null) {
            pluginDBManager = new PluginDBManager(crescentCore);
        }
    }

    public PlayerDBManager getPlayerDataManager() {
        return playerDBManager;
    }

    public PluginDBManager getPluginDataManager() {
        return pluginDBManager;
    }

    public List<String> getTableNames() {
        return List.copyOf(tableNames);
    }

    public List<String> getPlayerTableNames() {
        List<String> playerTableNames = new ArrayList<>();
        for (String tableName : tableNames) {
            if (tableName.endsWith("_player_data")) {
                playerTableNames.add(tableName);
            }
        }
        return playerTableNames;
    }

    public List<String> getPluginTableNames() {
        List<String> pluginTableNames = new ArrayList<>();
        for (String tableName : tableNames) {
            if (!tableName.endsWith("_player_data")) {
                pluginTableNames.add(tableName);
            }
        }
        return pluginTableNames;
    }

}