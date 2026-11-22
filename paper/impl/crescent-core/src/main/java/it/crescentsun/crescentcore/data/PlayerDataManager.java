package it.crescentsun.crescentcore.data;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.DataDefinition;
import it.crescentsun.api.crescentcore.data.DataEntry;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.data.player.PlayerDataService;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements PlayerDataService {
    private volatile Map<String, PlayerDataTableSchema> schemasByTable = Map.of();

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
        Map<String, PlayerDataTableSchema> schemas = new LinkedHashMap<>();
        for (String tableName : dbManager.getPlayerTableNames()) {
            String pluginName = TableNameUtil.extractPluginNameFromPlayerDataTable(tableName);
            List<PlayerDataColumn> columns = new ArrayList<>();
            List<PlayerDataColumn> persistentColumns = new ArrayList<>();
            for (Map.Entry<NamespacedKey, DataDefinition<?>> entry :
                    crescentCore.getPlayerDataRegistry().getPlayerDataDefinitionsForPlugin(pluginName).entrySet()) {
                PlayerDataColumn column = new PlayerDataColumn(
                        entry.getKey(), entry.getKey().getKey(), entry.getValue());
                columns.add(column);
                if (entry.getValue().isPersistent()) persistentColumns.add(column);
            }

            String selectColumns = columns.stream().map(PlayerDataColumn::columnName)
                    .collect(java.util.stream.Collectors.joining(", "));
            String selectQuery = "SELECT player_uuid"
                    + (selectColumns.isEmpty() ? "" : ", " + selectColumns)
                    + " FROM " + tableName + " WHERE player_uuid = ?";
            String upsertQuery = buildPlayerUpsertQuery(tableName, persistentColumns);
            schemas.put(tableName, new PlayerDataTableSchema(tableName,
                    List.copyOf(columns), List.copyOf(persistentColumns), upsertQuery, selectQuery));
        }
        schemasByTable = Collections.unmodifiableMap(schemas);
    }

    @Override
    public PlayerData getData(UUID uuid) {
        requireMainThread("read the player-data repository");
        return repository.get(uuid);
    }

    @Override
    public List<PlayerData> getAllData() {
        requireMainThread("read the player-data repository");
        return List.copyOf(repository.values());
    }

    @Override
    public CompletableFuture<PlayerData> saveDataAsync(UUID dataKey) {
        return onMainThread(() -> captureSaveSnapshot(dataKey)).thenCompose(snapshot -> {
            if (snapshot == null) return CompletableFuture.completedFuture(null);
            return dbManager.executeAsync(() -> {
                persistPlayerSnapshot(snapshot);
                return snapshot.uuid();
            }).thenCompose(uuid -> onMainThread(() -> repository.get(uuid)));
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadDataAsync(UUID dataKey) {
        return dbManager.executeAsync(() -> readPlayerDataValues(dataKey))
                .thenCompose(values -> onMainThread(() -> installPlayerData(dataKey, values)));
    }

    private static String buildPlayerUpsertQuery(String tableName, List<PlayerDataColumn> columns) {
        if (columns.isEmpty()) return null;
        String columnNames = columns.stream().map(PlayerDataColumn::columnName)
                .collect(java.util.stream.Collectors.joining(", "));
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        String updates = columns.stream()
                .map(column -> column.columnName() + " = VALUES(" + column.columnName() + ")")
                .collect(java.util.stream.Collectors.joining(", "));
        return "INSERT INTO " + tableName + " (player_uuid, " + columnNames + ") VALUES (?, " + placeholders + ")"
                + " ON DUPLICATE KEY UPDATE " + updates;
    }

    /**
     * Saves the player data to the database.
     *
     * @param uuid The UUID of the player whose data should be saved.
     * @return The saved PlayerData instance.
     */
    public PlayerData saveData(UUID uuid) {
        requireMainThread("save player data synchronously");
        PlayerDataSaveSnapshot snapshot = captureSaveSnapshot(uuid);
        if (snapshot == null) return null;
        persistPlayerSnapshot(snapshot);
        return repository.get(uuid);
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
        requireMainThread("save all player data synchronously");
        List<PlayerDataSaveSnapshot> snapshots = captureAllSaveSnapshots();
        persistPlayerSnapshots(snapshots);
        Map<UUID, PlayerData> saved = new HashMap<>();
        for (PlayerDataSaveSnapshot snapshot : snapshots) {
            PlayerData playerData = repository.get(snapshot.uuid());
            if (playerData != null) saved.put(snapshot.uuid(), playerData);
        }
        return Map.copyOf(saved);
    }

    public CompletableFuture<Map<UUID, PlayerData>> asyncSaveAllData() {
        return onMainThread(this::captureAllSaveSnapshots)
                .thenCompose(snapshots -> dbManager.executeAsync(() -> {
                    persistPlayerSnapshots(snapshots);
                    return snapshots.stream().map(PlayerDataSaveSnapshot::uuid).toList();
                }))
                .thenCompose(uuids -> onMainThread(() -> {
                    Map<UUID, PlayerData> saved = new HashMap<>();
                    for (UUID uuid : uuids) {
                        PlayerData playerData = repository.get(uuid);
                        if (playerData != null) saved.put(uuid, playerData);
                    }
                    return Map.copyOf(saved);
                }));
    }

    /**
     * Loads the player data from the database. If no data is found,
     * default data is created for the player instead.
     *
     * @param uuid The UUID of the player whose data should be loaded.
     * @return The loaded PlayerData instance, or null if the player data is not found.
     */
    public PlayerData loadData(UUID uuid) {
        requireMainThread("load player data synchronously");
        return installPlayerData(uuid, readPlayerDataValues(uuid));
    }

    private Map<NamespacedKey, Object> readPlayerDataValues(UUID uuid) {
        Map<NamespacedKey, Object> databaseValues = new HashMap<>();
        try (Connection connection = dbManager.getConnection()) {
            for (PlayerDataTableSchema schema : schemasByTable.values()) {
                try (PreparedStatement statement = connection.prepareStatement(schema.selectQuery())) {
                    statement.setString(1, uuid.toString());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            for (PlayerDataColumn column : schema.columns()) {
                                Object value = result.getObject(column.columnName());
                                if (value == null) {
                                    databaseValues.put(column.key(), column.definition().getDefaultValue());
                                } else {
                                    databaseValues.put(column.key(), value);
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read player data for " + uuid + " from the database.", e);
        }
        return databaseValues;
    }

    private PlayerData installPlayerData(UUID uuid, Map<NamespacedKey, Object> databaseValues) {
        requireMainThread("publish loaded player data");
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            crescentCore.getLogger().warning("Player with UUID " + uuid + " is no longer online. Skipping data load.");
            return null;
        }

        PlayerData playerData = new PlayerData(player, databaseValues, crescentCore.getPlayerDataRegistry());
        repository.put(uuid, playerData);
        if (databaseValues.isEmpty()) {
            crescentCore.getLogger().info("Player UUID " + uuid + " is new!");
        }
        crescentCore.getLogger().info("Player [" + player.getName() + "] (" + uuid + ") data loaded.");
        return playerData;
    }

    /**
     * Loads all player data from the database. If no data is found, default data is created for the player instead.
     * Only used on server startup to load data for all online players.
     *
     * @return A list of loaded PlayerData instances.
     */
    public List<PlayerData> loadAllData() {
        requireMainThread("load all player data synchronously");
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


    private PlayerDataSaveSnapshot captureSaveSnapshot(UUID uuid) {
        requireMainThread("snapshot player data for saving");
        PlayerData playerData = repository.get(uuid);
        if (playerData == null) {
            crescentCore.getLogger().warning("Player data for UUID " + uuid + " not found. Cannot save data.");
            return null;
        }
        return new PlayerDataSaveSnapshot(uuid, playerData.snapshotValuesForPersistence());
    }

    private List<PlayerDataSaveSnapshot> captureAllSaveSnapshots() {
        requireMainThread("snapshot all player data for saving");
        List<PlayerDataSaveSnapshot> snapshots = new ArrayList<>();
        repository.forEach((uuid, playerData) -> snapshots.add(
                new PlayerDataSaveSnapshot(uuid, playerData.snapshotValuesForPersistence())));
        return List.copyOf(snapshots);
    }

    private void persistPlayerSnapshot(PlayerDataSaveSnapshot snapshot) {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                prepareAndExecuteSaveStatement(snapshot, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save player data for " + snapshot.uuid() + ".", e);
        }
        crescentCore.getLogger().info("Player data saved for UUID " + snapshot.uuid() + " in "
                + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void persistPlayerSnapshots(List<PlayerDataSaveSnapshot> snapshots) {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                for (PlayerDataSaveSnapshot snapshot : snapshots) {
                    prepareAndExecuteSaveStatement(snapshot, connection);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save all player data.", e);
        }
        crescentCore.getLogger().info("All player data saved in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void prepareAndExecuteSaveStatement(PlayerDataSaveSnapshot snapshot, Connection connection) throws SQLException {
        for (PlayerDataTableSchema schema : schemasByTable.values()) {
            String query = schema.upsertQuery();
            if (query == null) {
                continue;
            }
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                int index = 1;
                statement.setString(index++, snapshot.uuid().toString());
                for (PlayerDataColumn column : schema.persistentColumns()) {
                    statement.setObject(index++, snapshot.values().get(column.key()));
                }
                statement.executeUpdate();
            }
        }
    }

    @Override
    public PlayerData removeData(UUID uuid) {
        requireMainThread("remove player data from the repository");
        return repository.remove(uuid);
    }

    @Override
    public void deleteData(UUID uuid) {
        requireMainThread("delete player data synchronously");
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
            throw new IllegalStateException("Failed to delete player data for " + uuid + ".", e);
        }
    }

    public void updatePlayerSessionData(Player player) {
        requireMainThread("update player session data");
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

    private <T> CompletableFuture<T> onMainThread(java.util.function.Supplier<T> operation) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return CompletableFuture.completedFuture(operation.get());
            } catch (Throwable error) {
                return CompletableFuture.failedFuture(error);
            }
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            Bukkit.getScheduler().runTask(crescentCore, () -> {
                try {
                    future.complete(operation.get());
                } catch (Throwable error) {
                    future.completeExceptionally(error);
                }
            });
        } catch (RuntimeException error) {
            future.completeExceptionally(error);
        }
        return future;
    }

    private static void requireMainThread(String operation) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot " + operation + " off the Paper server thread.");
        }
    }

    private record PlayerDataSaveSnapshot(UUID uuid, Map<NamespacedKey, Object> values) { }

    private record PlayerDataColumn(NamespacedKey key, String columnName,
                                    DataDefinition<?> definition) { }

    private record PlayerDataTableSchema(String tableName, List<PlayerDataColumn> columns,
                                         List<PlayerDataColumn> persistentColumns,
                                         String upsertQuery, String selectQuery) { }

}
