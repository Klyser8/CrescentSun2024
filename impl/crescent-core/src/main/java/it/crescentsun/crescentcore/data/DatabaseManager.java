package it.crescentsun.crescentcore.data;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataEntry;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final HikariDataSource dataSource;
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
        // Collect player data table names
        List<String> playerPluginNames = crescentCore.getPlayerDataRegistry().getRegisteredPluginNames();
        for (String pluginName : playerPluginNames) {
            String tableName = TableNameUtil.appendPlayerDataTablePrefix(pluginName);
            tableNames.add(tableName);
        }

        // Collect plugin data table names
        for (Class<? extends PluginData> dataClass : crescentCore.getPluginDataRegistry().getRegistry()) {
            String tableName = TableNameUtil.appendPluginDataTablePrefix(dataClass);
            tableNames.add(tableName);
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

        if (TableNameUtil.isPlayerDataTable(tableName)) {
            addPlayerPrimaryKeyAndColumns(tableName, columns);
        } else {
            addPluginPrimaryKeyAndColumns(tableName, columns);
        }

        query = query.replace("%COLUMN_LIST%", columns.toString().trim().replaceAll(",$", ""));
        return query;
    }

    private void addPlayerPrimaryKeyAndColumns(String tableName, StringBuilder columns) {
        columns.append(DatabaseNamespacedKeys.PLAYER_UUID.getKey()).append(" VARCHAR(36) NOT NULL PRIMARY KEY, ");

        String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
        Map<NamespacedKey, DataDefinition<?>> playerDataRegistry = crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName);

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

    private void addPluginPrimaryKeyAndColumns(String tableName, StringBuilder columns) {
        Class<? extends PluginData> dataClass = crescentCore.getPluginDataManager().getPluginDataClassFromRawTableName(tableName);
        if (dataClass == null) {
            crescentCore.getLogger().warning("Could not find plugin data class for " + tableName + " table");
            return;
        }

        Map<Field, DatabaseColumn> serializableFields = crescentCore.getPluginDataManager().getSerializableFields(dataClass);
        for (Field field : serializableFields.keySet()) {
            DatabaseColumn annotation = serializableFields.get(field);
            String columnName = annotation.columnName().toLowerCase();
            String sqlType = annotation.dataType().getSqlType();
            if (annotation.isPrimaryKey()) {
                columns.append(columnName).append(" ").append(sqlType).append(" NOT NULL PRIMARY KEY, ");
            } else {
                columns.append(columnName).append(" ").append(sqlType).append(", ");
            }
        }
    }

    private void updateAllTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String tableName : tableNames) {
                if (TableNameUtil.isPlayerDataTable(tableName)) {
                    updatePlayerDataTable(statement, tableName);
                } else {
                    updatePluginDataTable(statement, tableName);
                }
            }

        } catch (SQLException e) {
            crescentCore.getLogger().severe("Error updating tables: " + e.getMessage());
        }
    }

    private void updatePlayerDataTable(Statement statement, String tableName) throws SQLException {
        String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
        Map<NamespacedKey, DataDefinition<?>> playerDataRegistry = crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName);

        for (NamespacedKey namespacedKey : playerDataRegistry.keySet()) {
            String columnName = namespacedKey.getKey().toLowerCase();

            if (doesColumnExist(tableName, columnName)) {
                continue;
            }

            DataType dataType = playerDataRegistry.get(namespacedKey).getType();
            addColumn(tableName, columnName, dataType, false, statement);
        }
    }


    private void updatePluginDataTable(Statement statement, String tableName) throws SQLException {
        Class<? extends PluginData> dataClass = crescentCore.getPluginDataManager().getPluginDataClassFromRawTableName(tableName);
        if (dataClass == null) {
            crescentCore.getLogger().warning("Could not find plugin data class for " + tableName + " table");
            return;
        }

        Map<Field, DatabaseColumn> serializableFields = crescentCore.getPluginDataManager().getSerializableFields(dataClass);
        for (Field field : serializableFields.keySet()) {
            DatabaseColumn annotation = serializableFields.get(field);
            String columnName = annotation.columnName().toLowerCase();

            if (doesColumnExist(tableName, columnName)) {
                continue;
            }

            boolean isPrimaryKey = annotation.isPrimaryKey();
            addColumn(tableName, columnName, annotation.dataType(), isPrimaryKey, statement);
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
        crescentCore.getPlayerDataManager().saveAllData();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Could not establish a connection to the database");
        }
        return connection;
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public List<String> getTableNames() {
        return List.copyOf(tableNames);
    }

    public List<String> getPlayerTableNames() {
        List<String> playerTableNames = new ArrayList<>();
        for (String tableName : tableNames) {
            if (TableNameUtil.isPlayerDataTable(tableName)) {
                playerTableNames.add(tableName);
            }
        }
        return playerTableNames;
    }

    public List<String> getPluginTableNames() {
        List<String> pluginTableNames = new ArrayList<>();
        for (String tableName : tableNames) {
            if (TableNameUtil.isPlayerDataTable(tableName)) {
                continue;
            }
            pluginTableNames.add(tableName);
        }
        return pluginTableNames;
    }

}