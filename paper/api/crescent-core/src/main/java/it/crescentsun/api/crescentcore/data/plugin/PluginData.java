package it.crescentsun.api.crescentcore.data.plugin;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.DataNotFoundException;
import org.bukkit.plugin.java.JavaPlugin;
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
        return pluginDataService.asyncSaveDataAndSync(this).exceptionally(throwable -> {
            throw new CompletionException("Couldn't save and sync data " + this, throwable);
        });
    }

    /**
     * Deletes this PluginData instance from the database (asynchronously), to then be removed from other servers' repositories as well.
     * If the data couldn't be deleted and synced, it will be added back to the repository, and an exception will be thrown.
     *
     * @return The PluginDataIdentifier of the deleted data.
     * @param <T> The type of the PluginData.
     * @throws CompletionException If the data couldn't be deleted and synced.
     * @throws DataNotFoundException If the data couldn't be found in the repository.
     */
    @SuppressWarnings("unchecked")
    public <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> deleteAndSync() {
        // Async delete the data and handle errors
        PluginDataIdentifier<T> pluginIdentifier = (PluginDataIdentifier<T>) pluginDataService.removeData(this.getClass(), getUuid());
        if (pluginIdentifier != null ) {
            return pluginDataService.asyncDeleteDataAndSync(pluginIdentifier).exceptionally(throwable -> {
                pluginDataService.insertData(getUuid(), this, false);
                throw new CompletionException("Couldn't delete and sync data " + this, throwable);
            });
        } else {
            throw new DataNotFoundException("Failed to delete data " + this + " from repository.");
        }
    }

    /**
     * @return Whether the data instance has been initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Some Plugin Data implementations may require a connection to the proxy before being initialized.
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
}