package it.crescentsun.crescentcore.core.db;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.api.data.DataEntry;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static it.crescentsun.crescentcore.CrescentCore.PLAYER_DATA_ENTRY_REGISTRY;

/**
 * Manages player data, including loading, saving, updating, and creating default data.
 */
public class PlayerDBManager extends AbstractDataManager<UUID, PlayerData> {
    private final Map<String, String> INSERT_OR_UPDATE_PLAYER_DATA_QUERIES = new HashMap<>();
    private final Map<String, String> SELECT_PLAYER_DATA_QUERIES = new HashMap<>();

    /**
     * Constructs a new PlayerManager instance.
     *
     * @param crescentCore      The CrescentCore plugin instance.
     * @param dbManager   The DatabaseManager instance.
     */
    public PlayerDBManager(CrescentCore crescentCore, DatabaseManager dbManager) {
        super(crescentCore, dbManager);
        populatePlayerDataQueries();
        populateSelectPlayerDataQueries();
    }

    private void populatePlayerDataQueries() {
        for (String namespace : crescentCore.getDatabaseManager().getPlayerTableNames()) { //Should filter out tables that don't end in _player_data
            String primaryKey = "player_uuid";
            String query = "INSERT INTO %TABLENAME% (" + primaryKey + ", %COLUMN_LIST%) VALUES (?, %VALUE_PLACEHOLDERS%)" +
                    " ON DUPLICATE KEY UPDATE" +
                    " %ITERATE_COLUMNS%";
            query = query.replace("%TABLENAME%", namespace);
            StringBuilder columns = new StringBuilder();
            StringBuilder columnList = new StringBuilder();
            Map<NamespacedKey, DataEntry<?>> additionalData =
                    PLAYER_DATA_ENTRY_REGISTRY.getPlayerDataEntryForNamespace(namespace);
            // Iterate through the keyset to dictate column names
            for (NamespacedKey namespacedKey : additionalData.keySet()) {
                String columnName = namespacedKey.value();
                columns.append(columnName).append(" = VALUES(").append(columnName).append("), ");
                columnList.append(columnName).append(", ");
            }
            query = query.replace("%COLUMN_LIST%", columnList.toString().trim().replaceAll(",$", ""));
            query = query.replace("%VALUE_PLACEHOLDERS%", StringUtils.repeat(
                    "?, ", additionalData.size()).trim().replaceAll(",$", ""));
            query = query.replace("%ITERATE_COLUMNS%", columns.toString().trim().replaceAll(",$", ""));
            INSERT_OR_UPDATE_PLAYER_DATA_QUERIES.put(namespace, query);
        }
    }

    private void populateSelectPlayerDataQueries() {
        for (String namespace : crescentCore.getDatabaseManager().getPlayerTableNames()) {
            String primaryKey = "player_uuid";
            // Start building the SELECT query
            String query = "SELECT * FROM %TABLENAME% WHERE " + primaryKey + " = ?";

            // Finalize the query by replacing placeholders
            SELECT_PLAYER_DATA_QUERIES.put(namespace, query.replace("%TABLENAME%", namespace));
        }
    }

    /**
     * Saves the player data to the database.
     *
     * @param uuid The UUID of the player whose data should be saved.
     * @return The saved PlayerData instance.
     */
    @Override
    public PlayerData saveData(UUID uuid) {
        long startTime = System.currentTimeMillis();
        PlayerData playerData = getData(uuid);
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            prepareAndExecuteSaveStatement(uuid, connection, playerData);
            connection.commit();
            connection.setAutoCommit(true); // Restore autocommit mode
            crescentCore.getLogger().info("Player data saved for UUID: " + uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("Player [" + Bukkit.getOfflinePlayer(uuid).getName() + "] data saved in: " + timeElapsed + "ms");
        return playerData;
    }

    /**
     * Saves all player data to the database using batch processing and transactions.
     * This method optimizes the save process by grouping multiple SQL queries into
     * a single transaction, which can significantly improve performance when dealing
     * with a large number of players. This method should be called synchronously.
     * To save data asynchronously, use the asyncSaveAllData() method.
     *
     * @see #asyncSaveAllData()
     */
    @Override
    public Map<UUID, PlayerData> saveAllData() {
        long startTime = System.currentTimeMillis();
        Map<UUID, PlayerData> savedData = new HashMap<>();
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Assume getAllPlayerDataKeys() retrieves all player UUIDs
            for (UUID dataKey : dataMap.keySet()) {
                PlayerData playerData = getData(dataKey); // Retrieve each player's data
                prepareAndExecuteSaveStatement(dataKey, connection, playerData);
                savedData.put(dataKey, playerData);
            }
            connection.commit(); // Commit all changes in a single transaction
            connection.setAutoCommit(true); // Restore autocommit mode
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("All player data saved in: " + timeElapsed + "ms");
        return savedData;
    }

    public CompletableFuture<PlayerData> asyncSaveData(UUID dataKey) {
        return CompletableFuture.supplyAsync(() -> saveData(dataKey)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while saving player data: " + e.getMessage());
            return null;
        });
    }

    public CompletableFuture<Map<UUID, PlayerData>> asyncSaveAllData() {
        return CompletableFuture.supplyAsync(this::saveAllData).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while saving all player data: " + e.getMessage());
            return null;
        });
    }

    /**
     * Loads the player data from the database. If no data is found,
     * default data is created for the player instead.
     *
     * @param uuid The UUID of the player whose data should be loaded.
     * @return The loaded PlayerData instance, or null if the player data is not found.
     */
    @Override
    public PlayerData loadData(UUID uuid) {
        long startTime = System.currentTimeMillis();
        PlayerData playerData = new PlayerData(Bukkit.getPlayer(uuid));
        boolean hasData = false;
        try (Connection connection = dbManager.getConnection()) {
            for (NamespacedKey namespacedKey : playerData.getAllDataEntries().keySet()) {
                String query = SELECT_PLAYER_DATA_QUERIES.get(namespacedKey.namespace());
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, uuid.toString());
                    ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        playerData.updateDataValue(namespacedKey, result.getObject(namespacedKey.value()));
                        hasData = true;
                    }
                    result.close();
                }
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while loading player data: " + e.getMessage());
        }
        if (!hasData) {
            crescentCore.getLogger().info("Player UUID " + uuid + " is new! Setting default data...");
            setDefaultData(uuid, playerData);
        } else {
            crescentCore.getLogger().info("Player UUID " + uuid + " data loaded successfully!");
        }
        setData(uuid, playerData);
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("Player [" + Bukkit.getOfflinePlayer(uuid).getName() + "] data loaded in: " + timeElapsed + "ms");
        return playerData;
    }

    /**
     * Loads all player data from the database. If no data is found, default data is created for the player instead.
     * Only used on server startup to load data for all online players.
     *
     * @return
     * @see #asyncLoadAllData()
     */
    public CompletableFuture<Boolean> loadAllData() {
        long startTime = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData playerData = new PlayerData(player);
            boolean hasData = false;
            try (Connection connection = dbManager.getConnection()) {
                for (NamespacedKey namespacedKey : playerData.getAllDataEntries().keySet()) {
                    String query = SELECT_PLAYER_DATA_QUERIES.get(namespacedKey.namespace());
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, uuid.toString());
                        try (ResultSet result = statement.executeQuery()) {
                            if (result.next()) {
                                playerData.updateDataValue(namespacedKey, result.getObject(namespacedKey.value()));
                                hasData = true;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                crescentCore.getLogger().severe("An error occurred while loading data for all players: " + e.getMessage());
            }
            if (!hasData) {
                crescentCore.getLogger().info("Player UUID " + uuid + " is new! Setting default data...");
                setDefaultData(uuid, playerData);
            }
            setData(uuid, playerData); // Assuming setData updates the player data in your system
        }
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("All player data loaded in: " + timeElapsed + "ms");
        return CompletableFuture.completedFuture(true);
    }


    public void setDefaultData(UUID uuid, PlayerData playerData) {
        playerData.updateDataValue(
                CrescentNamespaceKeys.PLAYER_USERNAME, Bukkit.getOfflinePlayer(uuid).getName());
        playerData.updateDataValue(
                CrescentNamespaceKeys.PLAYER_FIRST_LOGIN, new Timestamp(System.currentTimeMillis()));
        playerData.updateDataValue(
                CrescentNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
    }

    public CompletableFuture<PlayerData> asyncLoadData(UUID dataKey) {
        return CompletableFuture.supplyAsync(() -> loadData(dataKey)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while loading player data: " + e.getMessage());
            return null;
        });
    }

    private void prepareAndExecuteSaveStatement(UUID dataKey, Connection connection, PlayerData playerData) throws SQLException {
        playerData.updateDataValue(CrescentNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
        for (NamespacedKey key : PLAYER_DATA_ENTRY_REGISTRY.getPlayerDataRegistry().keySet()) {
            String tableName = key.namespace();
            String query = INSERT_OR_UPDATE_PLAYER_DATA_QUERIES.get(tableName);
            if (query == null) {
                continue;
            }
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                Set<NamespacedKey> namespacedKeys = PLAYER_DATA_ENTRY_REGISTRY.getPlayerDataEntryForNamespace(tableName).keySet();
                int index = 0;
                statement.setString(++index, dataKey.toString()); // Set player UUID
                for (NamespacedKey namespacedKey : namespacedKeys) {
                    DataEntry<?> dataEntry = playerData.getDataEntry(namespacedKey);
                    setValueInStatement(namespacedKey, dataEntry, statement, ++index); // Set player data values
                }
                statement.executeUpdate(); // Execute the prepared statement for each player's player data
            }
        }
    }


    @SuppressWarnings("unused")
    public void asyncLoadAllData() {
        Bukkit.getScheduler().runTaskAsynchronously(crescentCore, this::loadAllData);
    }

    private void setValueInStatement(NamespacedKey namespacedKey, DataEntry<?> data, PreparedStatement statement, int index) throws SQLException {
        switch (data.getType()) {
            case INT -> statement.setInt(index, (int) data.getValue());
            case UNSIGNED_INT -> {
                if (!(data.getValue() instanceof Integer integer)) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (UNSIGNED_INT) must be an integer!");
                }
                if (integer < 0) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (UNSIGNED_INT) must be positive!");
                }
                statement.setInt(index, (int) data.getValue());
            }
            case DOUBLE -> statement.setDouble(index, (double) data.getValue());
            case FLOAT -> statement.setFloat(index, (float) data.getValue());
            case VARCHAR_16, NULLABLE_VARCHAR_16 -> {
                if (!(data.getValue() instanceof String string)) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_16) must be a string!");
                }
                if (string.length() > 16) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_16) is too long!");
                }
                statement.setString(index, (String) data.getValue());
            }
            case VARCHAR_36, NULLABLE_VARCHAR_36 -> {
                if (!(data.getValue() instanceof String string)) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_36) must be a string!");
                }
                if (string.length() > 36) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_36) is too long!");
                }
                statement.setString(index, (String) data.getValue());
            }
            case VARCHAR_255, NULLABLE_VARCHAR_255 -> {
                if (!(data.getValue() instanceof String string)) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_255) must be a string!");
                }
                if (string.length() > 255) {
                    throw new IllegalArgumentException("Value for " + namespacedKey + " (VARCHAR_255) is too long!");
                }
                statement.setString(index, (String) data.getValue());
            }
            case TIMESTAMP -> statement.setTimestamp(index, (Timestamp) data.getValue());
            case BOOLEAN -> statement.setBoolean(index, (boolean) data.getValue());
        }
    }
}