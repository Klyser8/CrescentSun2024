package it.crescentsun.crescentcore.core.data;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.plugindata.DataType;
import it.crescentsun.crescentcore.plugindata.PluginData;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.core.data.player.PlayerDataManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static it.crescentsun.crescentcore.CrescentCore.PLAYER_DATA_REGISTRY;

@SuppressWarnings("CallToPrintStackTrace")
public class DatabaseManager {

    private final HikariDataSource dataSource;
    private PlayerDataManager playerDataManager = null;
    private ServerDataManager serverDataManager = null;
    private final CrescentCore crescentCore;
    private final List<String> tableNamespaces = new ArrayList<>();
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
        // Populate tableNames
        for (NamespacedKey namespacedKey : PLAYER_DATA_REGISTRY.clonePluginDataRegistry().keySet()) {
            if (!tableNamespaces.contains(namespacedKey.namespace())) {
                tableNamespaces.add(namespacedKey.namespace());
            }
        }
        serverDataManager.createServerTable();
        serverDataManager.updateServerTable();
        createTables();
        updateAllTables();
    }

    /**
     * Creates the plugin tables for each plugin that has registered additional data.
     * The table name will be the name of the plugin.
     */
    private void createTables() {
        try (Statement statement = getConnection().createStatement()) {
            for (String namespace : tableNamespaces) {
                String query = populateCreateTableQuery(namespace);
                if (statement.executeUpdate(query) > 0) {
                    Bukkit.getLogger().info("Created " + namespace + " table");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        // Primary key should be id found in CrescentCore table
        columns.append(CrescentNamespaceKeys.PLAYER_UUID.getKey()).append(" VARCHAR(36) NOT NULL PRIMARY KEY, ");
        // username column
        Map<NamespacedKey, PluginData<?>> additionalDataRegistry = PLAYER_DATA_REGISTRY.getPluginDataForNamespace(tableName);
        for (NamespacedKey namespacedKey : additionalDataRegistry.keySet()) {
            String key = namespacedKey.value();
            Object value = additionalDataRegistry.get(namespacedKey).getValue();
            DataType dataType = DataType.fromObject(value);
            if (dataType == null) {
                Bukkit.getLogger().warning("Could not determine data type for " + key + " in " + tableName + " table");
                continue;
            }
            columns.append(key).append(" ").append(dataType.getSqlType()).append(", ");
        }
        query = query.replace("%COLUMN_LIST%", columns.toString().trim().replaceAll(",$", ""));
        return query;
    }

    private void updateAllTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String name : tableNamespaces) {
                updatePluginTable(statement, name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePluginTable(Statement statement, String namespace) throws SQLException {
        Map<NamespacedKey, PluginData<?>> pluginDataForNamespace = PLAYER_DATA_REGISTRY.getPluginDataForNamespace(namespace);

        // Iterate over each additional data entry
        for (NamespacedKey namespacedKey : pluginDataForNamespace.keySet()) {
            String tableName = namespacedKey.getNamespace();
            String columnName = namespacedKey.getKey();
//            System.out.println("Table name: " + tableName);
//            System.out.println("Column name: " + columnName);

            // Check if the column exists
            if (!doesColumnExist(namespacedKey)) {
                // Determine the SQL data type for the column based on the value's type
                Object value = pluginDataForNamespace.get(namespacedKey).getValue();
                DataType dataType = DataType.fromObject(value);
                if (dataType == null) {
                    Bukkit.getLogger().warning("Could not determine data type for " + columnName + " in " + tableName + " table");
                    continue;
                }
                String query = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType.getSqlType() + ";";

                // Execute the query to add the new column
                statement.executeUpdate(query);
                Bukkit.getLogger().info("Added column " + columnName + " to " + tableName + " table");
            }
        }
    }

    private boolean doesColumnExist(NamespacedKey namespacedKey) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData dbMetaData = connection.getMetaData();
            String tableName = namespacedKey.getNamespace();
            String columnName = namespacedKey.getKey();
            try (ResultSet rs = dbMetaData.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    Bukkit.getLogger().info("Column " + columnName + " exists in " + tableName + " table");
                    return true;
                } else {
                    Bukkit.getLogger().info("Column " + columnName + " does not exist in " + tableName + " table");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

/*    /**
     * Returns the column type for a value.
     * The column type will be determined based on the value's type.
     * If the value is a String, the column type will be VARCHAR, of varying lengths.
     *
     * @param value The value to get the column type for
     * @return The column type for the value
     */
/*    private static String getColumnType(Object value) {
        String columnType = "";
        if (value instanceof Integer) {
            columnType = "INT NOT NULL";
        } else if (value instanceof String string) {
            if (string.length() <= 16) {
                columnType = "VARCHAR(16) NOT NULL";
            } else if (string.length() <= 36) {
                columnType = "VARCHAR(36) NOT NULL";
            } else if (string.length() <= 255) {
                columnType = "VARCHAR(255) NOT NULL";
            }
        } else if (value instanceof Double) {
            columnType = "DOUBLE NOT NULL";
        } else if (value instanceof Boolean) {
            columnType = "BOOLEAN NOT NULL";
        } else if (value instanceof Timestamp) {
            columnType = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP";
        }
        return columnType;
    }*/

    public void saveEverything() {
        getPlayerDataManager().saveAllData();
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void initPlayerDataManager() {
        if (playerDataManager == null) {
            playerDataManager = new PlayerDataManager(crescentCore, this);
        }
    }

    public void initServerDataManager() {
        if (serverDataManager == null) {
            serverDataManager = new ServerDataManager(crescentCore, this);
        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    public ServerDataManager getServerDataManager() {
        return serverDataManager;
    }

    public List<String> getTableNamespaces() {
        return List.copyOf(tableNamespaces);
    }

}