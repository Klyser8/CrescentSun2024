package it.crescentsun.api.crescentcore.data.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing plugin data.
 * Here are found all methods that may be used to store, cache, retrieve and sync plugin data between backend servers.
 * Available are also default alternatives for common operations. <br>
 *
 * @see PluginDataRegistryService
 * @see PluginData
 * @see PluginDataIdentifier
 */
public interface PluginDataService {

    /**
     * Get a Plugin Data instance by its identifier.
     *
     * @param identifier The identifier of the data instance.
     * @param <T> The type of the data instance.
     * @return The data instance.
     */
    <T extends PluginData> T getData(PluginDataIdentifier<T> identifier);

    /**
     * Get a Plugin Data instance by its class and UUID.
     * @param dataClass The class of the data instance.
     * @param uuid The UUID of the data instance.
     * @param <T> The type of the data instance.
     * @return The data instance.
     */
    default <T extends PluginData> T getData(Class<T> dataClass, UUID uuid) {
        return getData(new PluginDataIdentifier<>(dataClass, uuid));
    }

    /**
     * Get all Plugin Data instances, independently of their type.
     * @return A map containing all data instances.
     */
    Map<PluginDataIdentifier<?>, PluginData> getAllData();

    /**
     * Get all Plugin Data instances of a specific type.
     * @param dataClass The class of the data instances.
     * @param <T> The type of the data instances.
     * @return A map containing all data instances of the specified type.
     */
    <T extends PluginData> Map<UUID, T> getAllDataOfType(Class<T> dataClass);

    /**
     * Inserts a plugin data instance to the repository (cache).
     * This method is not tied to any database operations. For those, see any of the asynchronous methods found below.
     * @param instanceUuid The UUID of the data instance.
     * @param dataInstance The data instance to be added.
     * @param shouldReplace Whether the data instance should replace an existing one with the same UUID.
     * @param <T> The type of the data instance.
     */
    <T extends PluginData> void insertData(UUID instanceUuid, T dataInstance, boolean shouldReplace);

    /**
     * Removes a plugin data instance from the repository (cache).
     * This method is also not tied to any database operations. For those, see any of the asynchronous methods found below.
     * @param identifier The identifier of the data instance.
     * @param <T> The type of the data instance.
     * @return The removed data instance.
     */
    <T extends PluginData> PluginDataIdentifier<T> removeData(PluginDataIdentifier<T> identifier);

    /**
     * Removes a plugin data instance from the repository (cache) by its class and UUID.
     * @see #removeData(PluginDataIdentifier)
     *
     * @param dataClass The class of the data instance.
     * @param uuid The UUID of the data instance.
     * @param <T> The type of the data instance.
     * @return The removed data instance.
     */
    default <T extends PluginData> PluginDataIdentifier<T> removeData(Class<T> dataClass, UUID uuid) {
        return removeData(new PluginDataIdentifier<>(dataClass, uuid));
    }

    /**
     * Captures the instance's annotated fields on the Paper server thread, then asynchronously writes
     * the plain values and a notification-outbox row in one SQL transaction. Core publishes the notification
     * after commit and retries it from the outbox if publication fails. The future completes exceptionally
     * if snapshotting, persistence, or the immediate notification attempt fails; a committed write remains durable.
     * @param pluginData The data instance to be saved and synced.
     * @param <T> The type of the data instance.
     * @return A future that completes with the original instance after SQL persistence and notification publication succeed.
     */
    <T extends PluginData> CompletableFuture<T> asyncSaveDataAndSync(@NotNull T pluginData);

    /**
     * Reads plain database values asynchronously, then constructs and caches the PluginData instance
     * on the Paper server thread. The future completes exceptionally if the read or materialization fails.
     *
     * @param identifier The identifier of the data instance.
     * @param <T> The type of the data instance.
     * @return A future that completes with the loaded instance, or {@code null} if no row exists.
     */
    <T extends PluginData> CompletableFuture<T> asyncLoadData(PluginDataIdentifier<T> identifier);

    /**
     * @see #asyncLoadData(PluginDataIdentifier)
     * @param dataClass The class of the data instance.
     * @param uuid The UUID of the data instance.
     * @param <T> The type of the data instance.
     * @return A future that will complete when the data is loaded.
     */
    default <T extends PluginData> CompletableFuture<T> asyncLoadData(Class<T> dataClass, UUID uuid) {
        return asyncLoadData(new PluginDataIdentifier<>(dataClass, uuid));
    }

    /**
     * Deletes a plugin data instance and enqueues its notification in one SQL transaction, then publishes
     * the notification so other backends can evict it from their caches. The future completes exceptionally
     * if the database operation or immediate notification attempt fails. If SQL committed, the local cache
     * entry is evicted and Core retries publication from the durable outbox.
     * @param identifier The identifier of the data instance.
     * @param <T> The type of the data instance.
     * @return A future that completes when the data is deleted and the notification is published.
     */
    <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> asyncDeleteDataAndSync(PluginDataIdentifier<T> identifier);

    /**
     * @see #asyncDeleteDataAndSync(PluginDataIdentifier)
     * @param dataClass The class of the data instance.
     * @param uuid The UUID of the data instance.
     * @param <T> The type of the data instance.
     * @return A future that will complete when the data is deleted.
     */
    default <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> asyncDeleteDataAndSync(Class<T> dataClass, UUID uuid) {
        return asyncDeleteDataAndSync(new PluginDataIdentifier<>(dataClass, uuid));
    }
}
