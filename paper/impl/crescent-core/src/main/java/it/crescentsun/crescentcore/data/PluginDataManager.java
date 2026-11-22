package it.crescentsun.crescentcore.data;

import com.google.common.collect.ImmutableMap;
import it.crescentsun.api.crescentcore.data.ClassNotRegisteredException;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.*;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import it.unimi.dsi.fastutil.Pair;
import it.crescentsun.crescentcore.messaging.PluginDataChange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PluginDataManager implements PluginDataService {
    private volatile Map<Class<? extends PluginData>, PluginDataSchema> schemasByClass = Map.of();
    private volatile Map<String, PluginDataSchema> schemasByTable = Map.of();

    private final Map<PluginDataIdentifier<? extends PluginData>, PluginData> repository = new ConcurrentHashMap<>();

    private final CrescentCore crescentCore;
    private final DatabaseManager dbManager;
    private final AtomicBoolean outboxDispatchInProgress = new AtomicBoolean();
    private BukkitTask outboxDispatchTask;

    public PluginDataManager(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        dbManager = crescentCore.getDatabaseManager();
    }

    public void init() {
        Map<Class<? extends PluginData>, PluginDataSchema> initializedSchemas = new LinkedHashMap<>();
        Map<String, PluginDataSchema> initializedSchemasByTable = new LinkedHashMap<>();
        for (PluginDataSchema schema : schemasByClass.values()) {
            PluginDataSchema initialized = schema.withQueries(
                    buildInsertOrUpdateQuery(schema), buildSelectQuery(schema));
            initializedSchemas.put(initialized.dataClass(), initialized);
            initializedSchemasByTable.put(initialized.tableName(), initialized);
        }
        schemasByClass = Collections.unmodifiableMap(initializedSchemas);
        schemasByTable = Collections.unmodifiableMap(initializedSchemasByTable);
    }

    public void startOutboxDispatcher() {
        requireMainThread("start the plugin-data notification dispatcher");
        if (outboxDispatchTask != null) return;
        outboxDispatchTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                crescentCore, this::dispatchPendingOutboxEvents, 20L, 20L * 30L);
        dispatchPendingOutboxEvents();
    }

    @Override
    public <T extends PluginData> CompletableFuture<T> asyncSaveDataAndSync(@NotNull T pluginData) {
        return onMainThread(() -> capturePluginData(pluginData)).thenCompose(snapshot ->
                dbManager.executeAsync(() -> persistPluginData(snapshot)))
                .thenCompose(this::publishOutboxEvent)
                .thenCompose(published -> onMainThread(() -> pluginData))
                .whenComplete((saved, error) -> {
                    if (error != null) crescentCore.getLogger().severe(
                            "Failed to save and announce plugin data: " + error.getMessage());
                });
    }

    @Override
    public <T extends PluginData> CompletableFuture<T> asyncLoadData(PluginDataIdentifier<T> identifier) {
        return dbManager.executeAsync(() -> readPluginDataRecord(identifier.classType(), identifier.uuid()))
                .thenCompose(record -> onMainThread(() -> {
                    PluginData data = materializePluginDataRecord(identifier.classType(), record);
                    return data == null ? null : identifier.classType().cast(data);
                }))
                .whenComplete((data, error) -> {
                    if (error != null) crescentCore.getLogger().severe(
                            "Failed to asynchronously load plugin data " + identifier + ": " + error.getMessage());
                });
    }

    @Override
    public <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> asyncDeleteDataAndSync(PluginDataIdentifier<T> identifier) {
        return dbManager.executeAsync(() -> deleteDataAndEnqueue(identifier.classType(), identifier.uuid()))
                .thenCompose(deleteResult -> {
            if (deleteResult == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("Plugin data was not deleted."));
            }
            return onMainThread(() -> {
                evictDataSilently(identifier);
                return deleteResult;
            });
        }).thenCompose(deleteResult -> publishOutboxEvent(deleteResult.outboxEntry())
                .thenApply(ignored -> deleteResult.identifier()))
                .whenComplete((deleted, error) -> {
                    if (error != null) crescentCore.getLogger().severe(
                            "Plugin-data deletion failed or its notification is pending retry: " + error.getMessage());
        });
    }

    /** Applies a remote notice using SQL as the source of truth. Call on the Paper main thread. */
    public void applyRemoteChange(PluginDataChange change) {
        requireMainThread("apply remote plugin-data changes");
        Class<? extends PluginData> dataClass = getPluginDataClassFromRawTableName(change.typeId());
        if (dataClass == null) {
            crescentCore.getLogger().warning("Ignoring update for unregistered plugin-data type " + change.typeId());
            return;
        }
        asyncLoadData(dataClass, change.recordId()).thenAccept(data -> {
            if (data == null) {
                evictDataSilently(new PluginDataIdentifier(dataClass, change.recordId()));
            } else {
                data.tryInit();
            }
        }).exceptionally(error -> {
            try {
                Bukkit.getScheduler().runTaskLater(crescentCore,
                        () -> applyRemoteChange(change), 20L * 30L);
            } catch (RuntimeException schedulingError) {
                crescentCore.getLogger().warning("Could not schedule plugin-data refresh retry: "
                        + schedulingError.getMessage());
            }
            return null;
        });
    }

    public void initializeRegisteredDataTypes(Collection<Class<? extends PluginData>> dataClasses,
                                              Collection<String> playerTableNames) {
        Map<Class<? extends PluginData>, PluginDataSchema> schemas = new LinkedHashMap<>();
        Map<String, PluginDataSchema> schemasByTable = new LinkedHashMap<>();
        Map<String, String> tableOwners = new HashMap<>();

        for (String playerTableName : playerTableNames) {
            tableOwners.put(playerTableName.toLowerCase(Locale.ROOT), "player-data registry");
        }
        tableOwners.put(DatabaseManager.PLUGIN_DATA_OUTBOX_TABLE.toLowerCase(Locale.ROOT), "Crescent Core notification outbox");

        Set<Class<? extends PluginData>> seenClasses = new HashSet<>();
        for (Class<? extends PluginData> dataClass : dataClasses) {
            if (!seenClasses.add(dataClass)) {
                throw new IllegalStateException("Plugin data class registered more than once: " + dataClass.getName());
            }
            if (Modifier.isAbstract(dataClass.getModifiers())) {
                throw new IllegalStateException("Plugin data class " + dataClass.getName()
                        + " must be concrete because Core instantiates it when loading records.");
            }

            String tableName = TableNameUtil.appendPluginDataTablePrefix(dataClass).toLowerCase(Locale.ROOT);
            validateSqlIdentifier(tableName, "table", dataClass.getName());
            String previousOwner = tableOwners.putIfAbsent(tableName, dataClass.getName());
            if (previousOwner != null) {
                throw new IllegalStateException("Duplicate database table '" + tableName + "' declared by "
                        + dataClass.getName() + "; it conflicts with " + previousOwner + ".");
            }

            Map<Field, DatabaseColumn> columns = findSerializableFields(dataClass);
            Pair<Field, DatabaseColumn> primaryKey = validatePluginDataSchema(dataClass, tableName, columns);
            try {
                dataClass.getDeclaredConstructor();
            } catch (NoSuchMethodException exception) {
                throw new IllegalStateException("Plugin data class " + dataClass.getName()
                        + " must declare a no-argument constructor for database loading.", exception);
            }

            Map<Field, DatabaseColumn> immutableColumns =
                    Collections.unmodifiableMap(new LinkedHashMap<>(columns));
            PluginDataSchema schema = new PluginDataSchema(dataClass, tableName, immutableColumns,
                    primaryKey, null, null);
            schemas.put(dataClass, schema);
            schemasByTable.put(tableName, schema);
        }

        this.schemasByClass = Collections.unmodifiableMap(schemas);
        this.schemasByTable = Collections.unmodifiableMap(schemasByTable);
    }

    @Override
    public <T extends PluginData> T getData(PluginDataIdentifier<T> pluginDataIdentifier) {
        requireMainThread("read the plugin-data repository");
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
        requireMainThread("read the plugin-data repository");
        return ImmutableMap.copyOf(repository);
    }

    @Override
    public <T extends PluginData> Map<UUID, T> getAllDataOfType(Class<T> dataClass) {
        requireMainThread("read the plugin-data repository");
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
        requireMainThread("publish plugin data to the repository");
        Class<? extends PluginData> dataClass = pluginData.getClass();
        if (schemasByClass.get(dataClass) == null) {
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
        requireMainThread("remove plugin data from the repository");
        Class<T> dataClass = identifier.classType();
        if (schemasByClass.get(dataClass) == null) {
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
        requireMainThread("save plugin data synchronously");
        PluginDataSnapshot snapshot = capturePluginData(pluginData);
        dispatchOutboxEvent(persistPluginData(snapshot));
        return pluginData;
    }

    @ApiStatus.Internal
    public <T extends PluginData> T loadData(Class<T> dataClass, UUID uuid) {
        requireMainThread("load plugin data synchronously");
        return dataClass.cast(materializePluginDataRecord(dataClass, readPluginDataRecord(dataClass, uuid)));
    }

    @ApiStatus.Internal
    public <T extends PluginData> PluginDataIdentifier<T> deleteData(Class<T> dataClass, UUID uuid) {
        requireMainThread("delete plugin data synchronously");
        PluginDataDeleteResult result = deleteDataAndEnqueue(dataClass, uuid);
        if (result == null) return null;
        evictDataSilently(result.identifier());
        dispatchOutboxEvent(result.outboxEntry());
        return result.identifier();
    }

    private <T extends PluginData> PluginDataDeleteResult<T> deleteDataAndEnqueue(Class<T> dataClass, UUID uuid) {
        PluginDataSchema schema = getSchema(dataClass);
        String tableName = schema.tableName();
        Pair<Field, DatabaseColumn> primaryKeyPair = schema.primaryKey();

        String queryTemplate = "DELETE FROM " + tableName + " WHERE " + primaryKeyPair.value().columnName() + " = ?";

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTemplate)) {

            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                // Lock the row we are about to delete
                lockPluginDataRow(connection, tableName, primaryKeyPair.value().columnName(), uuid);

                statement.setObject(1, uuid.toString());

                int affectedRows = statement.executeUpdate();
                if (affectedRows > 0) {
                    OutboxEntry outboxEntry = enqueueOutboxEvent(connection, PluginDataChange.Operation.DELETE,
                            tableName, uuid);
                    connection.commit();
                    crescentCore.getLogger().info("Successfully deleted data with primary key " + uuid + " from table " + tableName);
                    return new PluginDataDeleteResult<>(new PluginDataIdentifier<>(dataClass, uuid), outboxEntry);
                } else {
                    connection.commit();
                    crescentCore.getLogger().warning("No data found with primary key " + uuid + " in table " + tableName);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete plugin data " + dataClass.getName()
                    + " with UUID " + uuid + ".", e);
        }

        return null;
    }

    @ApiStatus.Internal
    public boolean saveAllData() {
        requireMainThread("save all plugin data synchronously");
        List<PluginDataSnapshot> snapshots = new ArrayList<>();
        for (PluginData data : repository.values()) snapshots.add(capturePluginData(data));
        try {
            for (OutboxEntry entry : persistPluginDataSnapshots(snapshots)) dispatchOutboxEvent(entry);
            return true;
        } catch (RuntimeException e) {
            crescentCore.getLogger().severe("An error occurred while saving all plugin data to the database: " + e.getMessage());
            return false;
        }
    }

    @ApiStatus.Internal
    public boolean loadAllData() {
        requireMainThread("load all plugin data synchronously");
        try {
            installPluginDataRecords(readAllPluginDataRecords());
            return true;
        } catch (Exception e) {
            crescentCore.getLogger().severe("An error occurred while loading all plugin data from the database: " + e.getMessage());
            return false;
        }
    }

    @ApiStatus.Internal
    public boolean reloadAllData() {
        requireMainThread("reload plugin data synchronously");
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
    public CompletableFuture<Boolean> asyncReloadAllData() {
        return dbManager.executeAsync(this::readAllPluginDataRecords)
                .thenCompose(records -> onMainThread(() -> {
                    clearAllData();
                    installPluginDataRecords(records);
                    for (PluginData data : repository.values()) data.tryInit();
                    return true;
                }))
                .whenComplete((loaded, error) -> {
                    if (error != null) crescentCore.getLogger().severe(
                            "Failed to asynchronously reload plugin data: " + error.getMessage());
                });
    }

    @ApiStatus.Internal
    public CompletableFuture<Boolean> asyncLoadAllData() {
        return dbManager.executeAsync(this::readAllPluginDataRecords)
                .thenCompose(records -> onMainThread(() -> {
                    installPluginDataRecords(records);
                    return true;
                }))
                .whenComplete((loaded, error) -> {
                    if (error != null) crescentCore.getLogger().severe(
                            "Failed to asynchronously load plugin data: " + error.getMessage());
                });
    }

    private static String buildInsertOrUpdateQuery(PluginDataSchema schema) {
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        StringJoiner updates = new StringJoiner(", ");
        for (DatabaseColumn column : schema.columns().values()) {
            columnNames.add(column.columnName());
            placeholders.add("?");
            if (!column.isPrimaryKey()) {
                updates.add(column.columnName() + " = VALUES(" + column.columnName() + ")");
            }
        }
        String query = "INSERT INTO " + schema.tableName() + " (" + columnNames + ") VALUES (" + placeholders + ")";
        if (updates.length() > 0) query += " ON DUPLICATE KEY UPDATE " + updates;
        return query;
    }

    private static String buildSelectQuery(PluginDataSchema schema) {
        StringJoiner columns = new StringJoiner(", ");
        for (DatabaseColumn column : schema.columns().values()) columns.add(column.columnName());
        return "SELECT " + columns + " FROM " + schema.tableName();
    }

    private PluginDataSnapshot capturePluginData(PluginData pluginData) {
        requireMainThread("snapshot plugin data for persistence");
        Class<? extends PluginData> dataClass = pluginData.getClass();
        PluginDataSchema schema = getSchema(dataClass);
        Pair<Field, DatabaseColumn> primaryKey = schema.primaryKey();
        try {
            Object key = primaryKey.key().get(pluginData);
            if (!(key instanceof UUID uuid) || !uuid.equals(pluginData.getUuid())) {
                throw new IllegalStateException("Primary key field and getUuid() disagree for " + dataClass.getName() + ".");
            }

            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<Field, DatabaseColumn> entry : schema.columns().entrySet()) {
                Object value = entry.getKey().get(pluginData);
                if (value instanceof Timestamp timestamp) value = new Timestamp(timestamp.getTime());
                values.put(entry.getValue().columnName(), value);
            }
            return new PluginDataSnapshot(dataClass, schema.tableName(), uuid,
                    Collections.unmodifiableMap(values));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not snapshot plugin data " + dataClass.getName() + ".", exception);
        }
    }

    private OutboxEntry persistPluginData(PluginDataSnapshot snapshot) {
        PluginDataSchema schema = getSchema(snapshot.dataClass());
        String query = schema.insertOrUpdateQuery();
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                lockPluginDataRow(connection, schema.tableName(), schema.primaryKey().value().columnName(), snapshot.uuid());
                bindPluginDataFields(statement, schema, snapshot.values());
                statement.executeUpdate();
                OutboxEntry outboxEntry = enqueueOutboxEvent(connection, PluginDataChange.Operation.UPSERT,
                        schema.tableName(), snapshot.uuid());
                connection.commit();
                return outboxEntry;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to persist plugin data " + snapshot.dataClass().getName()
                    + " with UUID " + snapshot.uuid() + ".", exception);
        }
    }

    private List<OutboxEntry> persistPluginDataSnapshots(List<PluginDataSnapshot> snapshots) {
        List<OutboxEntry> outboxEntries = new ArrayList<>();
        try (Connection connection = dbManager.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                for (PluginDataSnapshot snapshot : snapshots) {
                    PluginDataSchema schema = getSchema(snapshot.dataClass());
                    String query = schema.insertOrUpdateQuery();
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        lockPluginDataRow(connection, schema.tableName(), schema.primaryKey().value().columnName(), snapshot.uuid());
                        bindPluginDataFields(statement, schema, snapshot.values());
                        statement.executeUpdate();
                    }
                    outboxEntries.add(enqueueOutboxEvent(connection, PluginDataChange.Operation.UPSERT,
                            schema.tableName(), snapshot.uuid()));
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
            return List.copyOf(outboxEntries);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to persist all plugin data snapshots.", exception);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to persist all plugin data snapshots.", exception);
        }
    }

    private void bindPluginDataFields(PreparedStatement statement, PluginDataSchema schema,
                                      Map<String, Object> values) throws SQLException {
        int index = 1;
        for (Map.Entry<Field, DatabaseColumn> entry : schema.columns().entrySet()) {
            Object value = values.get(entry.getValue().columnName());
            if (value instanceof UUID uuid) {
                statement.setString(index++, uuid.toString());
            } else if (value instanceof Boolean booleanValue) {
                statement.setBoolean(index++, booleanValue);
            } else {
                statement.setObject(index++, value);
            }
        }
    }

    private OutboxEntry enqueueOutboxEvent(Connection connection, PluginDataChange.Operation operation,
                                           String tableName, UUID recordId) throws SQLException {
        UUID eventId = UUID.randomUUID();
        String query = "INSERT INTO " + DatabaseManager.PLUGIN_DATA_OUTBOX_TABLE
                + " (event_id, operation, type_id, record_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, eventId.toString());
            statement.setString(2, operation.name());
            statement.setString(3, tableName);
            statement.setString(4, recordId.toString());
            statement.executeUpdate();
        }
        return new OutboxEntry(eventId, new PluginDataChange(operation, tableName, recordId));
    }

    private CompletableFuture<Void> publishOutboxEvent(OutboxEntry entry) {
        try {
            return crescentCore.getMessagingService().publish(entry.change()).toCompletableFuture()
                    .thenCompose(ignored -> dbManager.<Void>executeAsync(() -> {
                        try {
                            deleteOutboxEvent(entry.eventId());
                        } catch (SQLException exception) {
                            throw new IllegalStateException("Failed to acknowledge plugin-data notification "
                                    + entry.eventId() + ".", exception);
                        }
                        return null;
                    }))
                    .whenComplete((ignored, error) -> {
                        if (error != null) crescentCore.getLogger().warning(
                                "Plugin-data notification " + entry.eventId() + " remains pending for retry: "
                                        + error.getMessage());
                    });
        } catch (Throwable error) {
            crescentCore.getLogger().warning("Plugin-data notification " + entry.eventId()
                    + " remains pending for retry: " + error.getMessage());
            return CompletableFuture.failedFuture(error);
        }
    }

    private void dispatchOutboxEvent(OutboxEntry entry) {
        publishOutboxEvent(entry);
    }

    private void dispatchPendingOutboxEvents() {
        if (!outboxDispatchInProgress.compareAndSet(false, true)) return;
        dbManager.executeAsync(this::readPendingOutboxEvents)
                .thenCompose(entries -> CompletableFuture.allOf(entries.stream()
                        .map(this::publishOutboxEvent)
                        .toArray(CompletableFuture[]::new)))
                .whenComplete((ignored, error) -> {
                    if (error != null) crescentCore.getLogger().warning(
                            "Some plugin-data notifications remain pending for retry: " + error.getMessage());
                    outboxDispatchInProgress.set(false);
                });
    }

    private List<OutboxEntry> readPendingOutboxEvents() {
        String query = "SELECT event_id, operation, type_id, record_id FROM "
                + DatabaseManager.PLUGIN_DATA_OUTBOX_TABLE + " ORDER BY created_at, event_id LIMIT 100";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            List<OutboxEntry> entries = new ArrayList<>();
            while (resultSet.next()) {
                UUID eventId = UUID.fromString(resultSet.getString("event_id"));
                PluginDataChange.Operation operation = PluginDataChange.Operation.valueOf(
                        resultSet.getString("operation"));
                String tableName = resultSet.getString("type_id");
                UUID recordId = UUID.fromString(resultSet.getString("record_id"));
                entries.add(new OutboxEntry(eventId,
                        new PluginDataChange(operation, tableName, recordId)));
            }
            return List.copyOf(entries);
        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Failed to read pending plugin-data notifications.", exception);
        }
    }

    private void deleteOutboxEvent(UUID eventId) throws SQLException {
        String query = "DELETE FROM " + DatabaseManager.PLUGIN_DATA_OUTBOX_TABLE + " WHERE event_id = ?";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, eventId.toString());
            statement.executeUpdate();
        }
    }

    private <T extends PluginData> void evictDataSilently(PluginDataIdentifier<T> identifier) {
        repository.remove(identifier);
    }

    private PluginDataRecord readPluginDataRecord(Class<? extends PluginData> dataClass, UUID uuid) {
        PluginDataSchema schema = getSchema(dataClass);
        String query = schema.selectQuery();

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     query + " WHERE " + schema.primaryKey().value().columnName() + " = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? readPluginDataRecord(dataClass, resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to read plugin data " + dataClass.getName() + " with UUID " + uuid + ".", exception);
        }
    }

    private List<PluginDataRecord> readAllPluginDataRecords() {
        List<PluginDataRecord> records = new ArrayList<>();
        try (Connection connection = dbManager.getConnection()) {
            for (String tableName : dbManager.getPluginTableNames()) {
                PluginDataSchema schema = schemasByTable.get(tableName);
                if (schema == null) throw new IllegalStateException("No schema registered for table " + tableName + ".");
                Class<? extends PluginData> dataClass = schema.dataClass();
                String query = schema.selectQuery();
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) records.add(readPluginDataRecord(dataClass, resultSet));
                }
            }
            return List.copyOf(records);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to read plugin data from the database.", exception);
        }
    }

    private PluginDataRecord readPluginDataRecord(Class<? extends PluginData> dataClass, ResultSet resultSet)
            throws SQLException {
        PluginDataSchema schema = getSchema(dataClass);
        Map<String, Object> values = new LinkedHashMap<>();
        UUID uuid = null;
        for (Map.Entry<Field, DatabaseColumn> entry : schema.columns().entrySet()) {
            Field field = entry.getKey();
            DatabaseColumn column = entry.getValue();
            Object value;
            if (field.getType() == UUID.class) {
                String raw = resultSet.getString(column.columnName());
                value = raw == null ? null : UUID.fromString(raw);
            } else {
                value = resultSet.getObject(column.columnName(), boxedType(field.getType()));
            }
            values.put(column.columnName(), value);
            if (column.isPrimaryKey() && value instanceof UUID id) uuid = id;
        }
        if (uuid == null) throw new IllegalStateException("Primary key is null while reading " + dataClass.getName() + ".");
        return new PluginDataRecord(dataClass, uuid, Collections.unmodifiableMap(values));
    }

    private PluginData materializePluginDataRecord(Class<? extends PluginData> dataClass, PluginDataRecord record) {
        requireMainThread("construct plugin data from a database record");
        if (record == null) return null;
        try {
            Constructor<? extends PluginData> constructor = dataClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            PluginData data = constructor.newInstance();
            for (Map.Entry<Field, DatabaseColumn> entry : getSchema(dataClass).columns().entrySet()) {
                entry.getKey().set(data, record.values().get(entry.getValue().columnName()));
            }
            insertData(record.uuid(), data, true);
            return data;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new IllegalStateException("Failed to construct plugin data " + dataClass.getName()
                    + " from database record " + record.uuid() + ".", exception);
        }
    }

    private void installPluginDataRecords(List<PluginDataRecord> records) {
        requireMainThread("publish loaded plugin data");
        for (PluginDataRecord record : records) materializePluginDataRecord(record.dataClass(), record);
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

    private record PluginDataSnapshot(Class<? extends PluginData> dataClass, String tableName, UUID uuid,
                                      Map<String, Object> values) { }
    private record PluginDataRecord(Class<? extends PluginData> dataClass, UUID uuid,
                                    Map<String, Object> values) { }
    private record OutboxEntry(UUID eventId, PluginDataChange change) { }
    private record PluginDataDeleteResult<T extends PluginData>(PluginDataIdentifier<T> identifier,
                                                                 OutboxEntry outboxEntry) { }
    private record PluginDataSchema(Class<? extends PluginData> dataClass, String tableName,
                                    Map<Field, DatabaseColumn> columns,
                                    Pair<Field, DatabaseColumn> primaryKey,
                                    String insertOrUpdateQuery, String selectQuery) {
        private PluginDataSchema withQueries(String insertOrUpdateQuery, String selectQuery) {
            return new PluginDataSchema(dataClass, tableName, columns, primaryKey,
                    insertOrUpdateQuery, selectQuery);
        }
    }

    private Pair<Field, DatabaseColumn> validatePluginDataSchema(
            Class<? extends PluginData> dataClass,
            String tableName,
            Map<Field, DatabaseColumn> columns) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Plugin data class " + dataClass.getName()
                    + " has no fields annotated with @DatabaseColumn.");
        }

        Map<String, Field> columnOwners = new HashMap<>();
        Map<Integer, Field> orderOwners = new HashMap<>();
        List<Pair<Field, DatabaseColumn>> primaryKeys = new ArrayList<>();
        for (Map.Entry<Field, DatabaseColumn> entry : columns.entrySet()) {
            Field field = entry.getKey();
            DatabaseColumn column = entry.getValue();
            String columnName = column.columnName().toLowerCase(Locale.ROOT);
            validateSqlIdentifier(columnName, "column", dataClass.getName() + "." + field.getName());

            Field previousColumn = columnOwners.putIfAbsent(columnName, field);
            if (previousColumn != null) {
                throw new IllegalStateException("Duplicate database column '" + columnName + "' in "
                        + dataClass.getName() + " on fields " + previousColumn.getName() + " and " + field.getName() + ".");
            }
            Field previousOrder = orderOwners.putIfAbsent(column.order(), field);
            if (previousOrder != null) {
                throw new IllegalStateException("Duplicate @DatabaseColumn order " + column.order() + " in "
                        + dataClass.getName() + " on fields " + previousOrder.getName() + " and " + field.getName() + ".");
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException("Database column " + dataClass.getName() + "." + field.getName()
                        + " cannot be static.");
            }
            if (!isCompatibleFieldType(field.getType(), column.dataType())) {
                throw new IllegalStateException("Database column " + dataClass.getName() + "." + field.getName()
                        + " has Java type " + field.getType().getTypeName() + " but declares SQL type "
                        + column.dataType() + ".");
            }
            if (column.isPrimaryKey()) {
                primaryKeys.add(Pair.of(field, column));
            }
        }

        if (primaryKeys.size() != 1) {
            throw new IllegalStateException("Plugin data class " + dataClass.getName()
                    + " must declare exactly one @DatabaseColumn primary key; found " + primaryKeys.size() + ".");
        }
        Pair<Field, DatabaseColumn> primaryKey = primaryKeys.getFirst();
        if (primaryKey.key().getType() != UUID.class || primaryKey.value().dataType() != DataType.VARCHAR_36) {
            throw new IllegalStateException("Primary key " + dataClass.getName() + "." + primaryKey.key().getName()
                    + " must be a UUID stored as VARCHAR_36.");
        }
        return primaryKey;
    }

    private static boolean isCompatibleFieldType(Class<?> fieldType,
                                                 it.crescentsun.api.crescentcore.data.DataType dataType) {
        if (fieldType == UUID.class) {
            return dataType == DataType.VARCHAR_36 || dataType == DataType.NULLABLE_VARCHAR_36;
        }
        return boxedType(fieldType).equals(dataType.getTypeClass());
    }

    private static Class<?> boxedType(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static void validateSqlIdentifier(String identifier, String kind, String owner) {
        if (identifier.length() > 64 || !identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalStateException("Invalid database " + kind + " '" + identifier + "' declared by "
                    + owner + ". Identifiers must match [A-Za-z_][A-Za-z0-9_]* and be at most 64 characters.");
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
        return getSchema(dataClass).tableName();
    }

    /**
     * Retrieves the plugin data class corresponding to the given full table name.
     * The full table name is expected to be in the format "pluginName_tableName".
     *
     * @param tableName The full table name in the format "pluginName_tableName".
     * @return The class of the plugin data that matches the given full table name, or null if no match is found.
     */
    public Class<? extends PluginData> getPluginDataClassFromRawTableName(String tableName) {
        PluginDataSchema schema = schemasByTable.get(tableName.toLowerCase(Locale.ROOT));
        return schema == null ? null : schema.dataClass();
    }

    /**
     * Retrieves the primary key field and its corresponding DatabaseColumn annotation from the given plugin data class.
     *
     * @param dataClass The class of the plugin data.
     * @return A Pair containing the primary key field and its DatabaseColumn annotation, or null if no primary key is found.
     */
    public Pair<Field, DatabaseColumn> getPrimaryKeyField(Class<? extends PluginData> dataClass) {
        PluginDataSchema schema = schemasByClass.get(dataClass);
        return schema == null ? null : schema.primaryKey();
    }

    @NotNull
    public Map<Field, DatabaseColumn> getSerializableFields(Class<? extends PluginData> dataClass) {
        return getSchema(dataClass).columns();
    }

    private PluginDataSchema getSchema(Class<? extends PluginData> dataClass) {
        PluginDataSchema schema = schemasByClass.get(dataClass);
        if (schema == null) {
            throw new ClassNotRegisteredException("Plugin data class is not registered: " + dataClass.getName());
        }
        return schema;
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

    /**
     * Locks a specific row for update to avoid concurrent modifications.
     */
    private void lockPluginDataRow(Connection connection, String tableName, String primaryKeyColumn, UUID uuid) throws SQLException {
        String lockQuery = "SELECT " + primaryKeyColumn + " FROM " + tableName + " WHERE " + primaryKeyColumn + " = ? FOR UPDATE";
        try (PreparedStatement lockStmt = connection.prepareStatement(lockQuery)) {
            lockStmt.setString(1, uuid.toString());
            lockStmt.executeQuery();
        }
    }

    public void clearDataOfType(Class<? extends PluginData> classType) {
        repository.entrySet().removeIf(entry -> entry.getKey().classType().equals(classType));
    }

    @ApiStatus.Internal
    public void clearAllData() {
        repository.clear();
    }

}
