package it.crescentsun.api.crescentcore.data.plugin;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.DataNotFoundException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Abstract class for plugin data that can be saved to a database.
 * All plugin data classes must extend this class.
 * All classes extending this one, must be annotated with {@link DatabaseTable}, and any fields that should be considered columns in the table should be annotated using {@link DatabaseColumn}.
 * It is crucial for your class to have a no-parameter constructor, for reflection purposes when it comes to instantiating it.
 *
 *
 * @see PluginDataService
 * @see PluginDataRegistryService
 * @see DatabaseTable
 * @see DatabaseColumn
 * @see DataType
 */
public abstract class PluginData {

    protected static PluginDataService pluginDataService;

    /** The plugin instance that owns this data. Populated after deserialization. */
    protected transient CrescentPlugin owningPlugin;
    /** Whether this data instance has been initialized. */
    protected transient boolean initialized = false;

    protected PluginData() {
        requireMainThread("construct plugin data");
        // Check that any subclass is annotated with @DatabaseTable
        if (!this.getClass().isAnnotationPresent(DatabaseTable.class)) { //TODO test
            throw new IllegalStateException("Plugin data class " + this.getClass().getName() + " should be annotated with @DatabaseTable!");
        }
        resolveOwningPlugin();
    }

    /**
     * This method shall be overridden in the subclass to return the UUID of the data instance.
     * @return The UUID of the data instance.
     */
    public abstract UUID getUuid();

    /**
     * Attempts to initialize the data instance. This method should be overridden in the subclass to perform any additional necessary initialization. <br>
     * <b>IMPORTANT: Whether this instance gets initialized or not SHOULD depend on the subclass' implementation of {@link #shouldInit()}</b>.
     * Call that before calling this method. <br> <br>
     * Data is initialized automatically by CrescentCore, with two attempts being made. The first takes place right after all data's been cached from the database, and the second after
     * a connection to the proxy has been established. <br>
     *
     * @param pluginDataService The PluginDataService instance to use for data operations.
     */
    public boolean tryInit() {
        requireMainThread("initialize plugin data");
        if (shouldInit()) {
            initialized = true;
            if (pluginDataService.getData(this.getClass(), getUuid()) == null) {
                pluginDataService.insertData(getUuid(), this, false);
            }
        }
        return initialized;
    }

    /**
     * This method should be overridden in the subclass to determine whether the data instance should be initialized.
     */
    protected abstract boolean shouldInit();

    /**
     * Saves this PluginData instance to the database (asynchronously), to then be synced with other servers' repositories.
     *
     * @return The saved PluginData instance.
     * @throws CompletionException If the data couldn't be saved and synced.
     */
    public CompletableFuture<PluginData> saveAndSync() {
        requireMainThread("save plugin data");
        String description = getClass().getName() + "[" + getUuid() + "]";
        return pluginDataService.asyncSaveDataAndSync(this).exceptionally(throwable -> {
            throw new CompletionException("Couldn't save and sync data " + description, throwable);
        });
    }

    /**
     * Deletes this PluginData instance from the database (asynchronously), to then be removed from other servers' repositories as well.
     * The local repository entry is removed after the database delete commits. Core records the notification
     * in a durable outbox in that same transaction and retries publication if the immediate Redis publish fails.
     *
     * @return The PluginDataIdentifier of the deleted data.
     * @param <T> The type of the PluginData.
     * @throws CompletionException If the database delete or immediate notification publication fails. If SQL committed,
     *                             the local entry is already evicted and the notification remains queued for retry.
     * @throws DataNotFoundException If the data couldn't be found in the repository.
     */
    @SuppressWarnings("unchecked")
    public <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> deleteAndSync() {
        requireMainThread("delete plugin data from the local repository");
        PluginDataIdentifier<T> pluginIdentifier = new PluginDataIdentifier<>((Class<T>) this.getClass(), getUuid());
        String description = getClass().getName() + "[" + pluginIdentifier.uuid() + "]";
        if (pluginDataService.getData(pluginIdentifier) == null) {
            throw new DataNotFoundException("Failed to delete data " + description + " from repository.");
        }
        return pluginDataService.asyncDeleteDataAndSync(pluginIdentifier).exceptionally(throwable -> {
            throw new CompletionException("Couldn't delete and sync data " + description, throwable);
        });
    }

    /**
     * @return Whether the data instance has been initialized.
     */
    public boolean isInitialized() {
        requireMainThread("read plugin-data initialization state");
        return initialized;
    }

    /**
     * Some Plugin Data implementations may require a connection to the proxy before being initialized.
     * For example, if a Plugin Data class requires fetching the name of the server it is currently on, that requires a proxy connection.
     *
     * To ensure correct initialization, it's important for the implementation to override this method and return true if the data instance is dependent on a proxy connection.<br>
     * If it is, the data instance will be initialized after a connection to the proxy has been established.<br>
     * If it isn't, the data instance will be initialized right after all data's been cached from the database.
     *
     * @return Whether the data instance is dependent on a proxy connection.
     */
    public abstract boolean isProxyDependent();

    /**
     * Populates the {@link #owningPlugin} field based on the {@link DatabaseTable}
     * annotation present on the plugin data implementation.
     */
    @ApiStatus.Internal
    public void resolveOwningPlugin() {
        requireMainThread("resolve a plugin-data owner");
        if (owningPlugin != null) return;
        if (this.getClass().isAnnotationPresent(DatabaseTable.class)) {
            Class<? extends CrescentPlugin> pluginClass = this.getClass()
                    .getAnnotation(DatabaseTable.class).plugin();

            this.owningPlugin = CrescentPlugin.getPlugin(pluginClass);
        }
    }

    /**
     * Sets the PluginDataService instance to be used by all PluginData instances.
     */
    @ApiStatus.Internal
    public static void setPluginDataService(PluginDataService pluginDataService) {
        PluginData.pluginDataService = pluginDataService;
    }

    private static void requireMainThread(String operation) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot " + operation + " off the Paper server thread.");
        }
    }
}
