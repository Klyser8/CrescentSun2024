package it.crescentsun.crescentcore.data;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataEntry;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.data.player.PlayerDataService;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements PlayerDataService {
    private final Map<String, String> INSERT_OR_UPDATE_PLAYER_DATA_QUERIES = new HashMap<>();
    private final Map<String, String> SELECT_PLAYER_DATA_QUERIES = new HashMap<>();

    private final Map<UUID, PlayerData> repository = new ConcurrentHashMap<>();

    private final CrescentCore crescentCore;

    private final DatabaseManager dbManager;

    /**
     * Constructs a new PlayerManager instance.
     *
     * @param crescentCore      The CrescentCore plugin instance.
     */
    public PlayerDataManager(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        this.dbManager = crescentCore.getDatabaseManager();
    }

    public void init() {
        populatePlayerDataQueries();
        populateSelectPlayerDataQueries();
    }

    @Override
    public PlayerData getData(UUID uuid) {
        return repository.get(uuid);
    }

    @Override
    public List<PlayerData> getAllData() {
        return (List<PlayerData>) repository.values();
    }

    @Override
    public CompletableFuture<PlayerData> saveDataAsync(UUID dataKey) {
        return CompletableFuture.supplyAsync(() -> saveData(dataKey)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while saving player data: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadDataAsync(UUID dataKey) {
        return CompletableFuture.supplyAsync(() -> loadData(dataKey)).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while loading player data: " + e.getMessage());
            return null;
        });
    }

    private void populatePlayerDataQueries() {
        for (String tableName : crescentCore.getDatabaseManager().getPlayerTableNames()) {
            String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
            String primaryKey = "player_uuid";
            String query = "INSERT INTO %TABLENAME% (" + primaryKey + ", %COLUMN_LIST%) VALUES (?, %VALUE_PLACEHOLDERS%)" +
                    " ON DUPLICATE KEY UPDATE" +
                    " %ITERATE_COLUMNS%";
            query = query.replace("%TABLENAME%", tableName);
            StringBuilder columns = new StringBuilder();
            StringBuilder columnList = new StringBuilder();
            Map<NamespacedKey, DataDefinition<?>> playerDataDefinitions =
                    crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName);
            // Iterate through the definitions to include only persistent columns
            int persistentCount = 0;
            for (Map.Entry<NamespacedKey, DataDefinition<?>> entry : playerDataDefinitions.entrySet()) {
                DataDefinition<?> dataDefinition = entry.getValue();
                if (!dataDefinition.isPersistent()) {
                    continue;
                }
                String columnName = entry.getKey().getKey();
                columns.append(columnName).append(" = VALUES(").append(columnName).append("), ");
                columnList.append(columnName).append(", ");
                persistentCount++;
            }
            // Handle case where there are no persistent columns
            if (persistentCount == 0) {
                // Skip adding query if there are no persistent columns
                continue;
            }
            query = query.replace("%COLUMN_LIST%", columnList.toString().trim().replaceAll(",$", ""));
            query = query.replace("%VALUE_PLACEHOLDERS%", StringUtils.repeat(
                    "?, ", persistentCount).trim().replaceAll(",$", ""));
            query = query.replace("%ITERATE_COLUMNS%", columns.toString().trim().replaceAll(",$", ""));
            INSERT_OR_UPDATE_PLAYER_DATA_QUERIES.put(tableName, query);
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
    public PlayerData saveData(UUID uuid) {
        long startTime = System.currentTimeMillis();
        PlayerData playerData = repository.get(uuid);
        if (playerData == null) {
            crescentCore.getLogger().warning("Player data for UUID " + uuid + " not found. Cannot save data.");
            return null;
        }
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                prepareAndExecuteSaveStatement(uuid, connection, playerData);
                connection.commit();
                crescentCore.getLogger().info("Player data saved for UUID: " + uuid);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true); // Restore autocommit mode
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while saving player data for " + playerData.getPlayer().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error occurred while processing database operation", e);
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
    public Map<UUID, PlayerData> saveAllData() {
        long startTime = System.currentTimeMillis();
        Map<UUID, PlayerData> savedData = new HashMap<>();
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false); // Start transaction
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                for (UUID dataKey : repository.keySet()) {
                    PlayerData playerData = repository.get(dataKey);
                    prepareAndExecuteSaveStatement(dataKey, connection, playerData);
                    savedData.put(dataKey, playerData);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true); // Restore autocommit mode
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while saving all player data: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            throw new RuntimeException("Error occurred while processing database operation", e);
        }
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("All player data saved in: " + timeElapsed + "ms");
        return savedData;
    }

    public CompletableFuture<Map<UUID, PlayerData>> asyncSaveAllData() {
        return CompletableFuture.supplyAsync(this::saveAllData).exceptionally(e -> {
            crescentCore.getLogger().severe("An error occurred while saving all player data: " + e.getMessage());
            e.printStackTrace();
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
    public PlayerData loadData(UUID uuid) {
        long startTime = System.currentTimeMillis();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            crescentCore.getLogger().warning("Player with UUID " + uuid + " is not online. Cannot load data.");
            return null;
        }
        Map<NamespacedKey, Object> databaseValues = new HashMap<>();
        try (Connection connection = dbManager.getConnection()) {
            for (String tableName : dbManager.getPlayerTableNames()) {
                String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
                String query = SELECT_PLAYER_DATA_QUERIES.get(tableName);
                if (query == null) {
                    crescentCore.getLogger().warning("No SELECT query found for table " + tableName);
                    continue;
                }
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, uuid.toString());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            for (NamespacedKey namespacedKey : crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName).keySet()) {
                                DataDefinition<?> dataDefinition = crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName).get(namespacedKey);
                                String columnName = namespacedKey.getKey();
                                Object value = result.getObject(columnName);
                                if (value == null) {
                                    databaseValues.put(namespacedKey, dataDefinition.getDefaultValue());
                                } else {
                                    databaseValues.put(namespacedKey, value);
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while loading data for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            throw new RuntimeException("Error occurred while processing database operation", e);
        }

        PlayerData playerData;

        //crystals:crystals exists, so crescentcore:* doesn't get created.

        if (databaseValues.isEmpty()) {
            playerData = new PlayerData(player, databaseValues, crescentCore.getPlayerDataRegistry());
            crescentCore.getLogger().info("Player UUID " + uuid + " is new!");
        } else {
            playerData = new PlayerData(player, databaseValues, crescentCore.getPlayerDataRegistry());
        }

        repository.put(uuid, playerData);
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("Player [" + player.getName() + "] ( " + player.getUniqueId() + ") data loaded in: " + timeElapsed + "ms");
        return playerData;
    }

    /**
     * Loads all player data from the database. If no data is found, default data is created for the player instead.
     * Only used on server startup to load data for all online players.
     *
     * @return A list of loaded PlayerData instances.
     */
    public List<PlayerData> loadAllData() {
        List<PlayerData> loadedData = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = loadData(player.getUniqueId());
            if (playerData == null) { // Player is not online. Skip.
                continue;
            }
            loadedData.add(playerData);
        }
        long timeElapsed = System.currentTimeMillis() - startTime;
        crescentCore.getLogger().info("All player data loaded in: " + timeElapsed + "ms");
        return loadedData;
    }


    private void prepareAndExecuteSaveStatement(UUID dataKey, Connection connection, PlayerData playerData) throws SQLException {
        for (String tableName : dbManager.getPlayerTableNames()) {
            String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
            String query = INSERT_OR_UPDATE_PLAYER_DATA_QUERIES.get(tableName);
            if (query == null) {
                continue;
            }
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                Set<NamespacedKey> namespacedKeys = crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName).keySet();
                int index = 1;
                statement.setString(index++, dataKey.toString()); // Set player UUID
                for (NamespacedKey namespacedKey : namespacedKeys) {
                    DataDefinition<?> dataDefinition = crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName).get(namespacedKey);
                    // Skip non-persistent data
                    try {
                        statement.setObject(index++, playerData.getDataValue(namespacedKey).orElse(dataDefinition.getDefaultValue()));
                    } catch (SQLException e) {
                        crescentCore.getLogger().severe("An error occurred while setting value for " + namespacedKey + ": " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                statement.executeUpdate(); // Execute the prepared statement for each player's player data
            }
        }
    }

    @Override
    public PlayerData removeData(UUID uuid) {
        return repository.remove(uuid);
    }

    @Override
    public void deleteData(UUID uuid) {
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                for (String tableName : dbManager.getPlayerTableNames()) {
                    String query = "DELETE FROM " + tableName + " WHERE player_uuid = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        // Lock the row to be deleted
                        lockPlayerDataRow(connection, tableName, uuid);

                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            crescentCore.getLogger().severe("An error occurred while deleting " + Bukkit.getPlayer(uuid).getName() +  "'s data: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            throw new RuntimeException("Error occurred while processing database operation", e);
        }
    }

    public void updatePlayerSessionData(Player player) {
        PlayerData playerData = getData(player.getUniqueId());
        if (playerData == null) {
            // Handle the case where playerData is not available
            return;
        }

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_LAST_SEEN, currentTime);

        Optional<Timestamp> loginTime = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_LAST_LOGIN);
        if (loginTime.isPresent()) {
            long playTime = currentTime.getTime() - loginTime.get().getTime();
            long totalPlayTime = playTime + (long) playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_PLAY_TIME).orElse(0L);
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_PLAY_TIME, totalPlayTime);
        } else {
            // If loginTime is not present, you might want to handle this case
            crescentCore.getLogger().warning("Login time not found for player: " + player.getName());
        }
    }

    /**
     * Acquire a row level lock on the player's data to avoid concurrent updates.
     */
    private void lockPlayerDataRow(Connection connection, String tableName, UUID uuid) throws SQLException {
        String lockQuery = "SELECT player_uuid FROM " + tableName + " WHERE player_uuid = ? FOR UPDATE";
        try (PreparedStatement lockStatement = connection.prepareStatement(lockQuery)) {
            lockStatement.setString(1, uuid.toString());
            lockStatement.executeQuery();
        }
    }

}