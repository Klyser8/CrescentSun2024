package it.crescentsun.crescentcore.api.data.plugin;

import it.crescentsun.crescentcore.CrescentCore;
import me.mrnavastar.protoweaver.api.netty.Sender;

import java.util.UUID;

/**
 * Abstract class for plugin data that can be saved to a database.
 * All plugin data classes must extend this class.
 * All classes extending this one, must be annotated with {@link DatabaseTable}, and any fields that should be considered columns in the table should be annotated using {@link DatabaseColumn}.
 * It is crucial for your class to have a no-parameter constructor, for reflection purposes when it comes to instantiating it.
 *
 *
 * @see DatabaseTable
 * @see DatabaseColumn
 * @see PluginDataRegistry
 * @see PluginDataRepository
 * @see it.crescentsun.crescentcore.api.data.DataType
 */
public abstract class PluginData {

    protected static final CrescentCore crescentCore = CrescentCore.getInstance();
    protected boolean initialized = false;

    protected PluginData() {
        // Check that any subclass is annotated with @DatabaseTable
        if (!this.getClass().isAnnotationPresent(DatabaseTable.class)) { //TODO test
            throw new IllegalStateException("Plugin data class " + this.getClass().getName() + " should be annotated with @DatabaseTable!");
        }
        // Register the subclass with the serializer if not already registered
    }

    public abstract UUID getUuid();

    /**
     * Attempts to initialize the data instance. This method should be overridden in the subclass to perform any additional necessary initialization. <br>
     * <b>IMPORTANT: Whether this instance gets initialized or not SHOULD depend on the subclass' implementation of {@link #shouldInit()}</b>.
     * Call that before calling this method.
     */
    public void init() {
        initialized = true;
        if (crescentCore.getPluginDataRepository().getData(this.getClass(), getUuid()) == null) {
            crescentCore.getPluginDataRepository().addDataInstance(this.getClass(), getUuid(), this);
        }
    }

    /**
     * This method should be overridden in the subclass to determine whether the data instance should be initialized.
     */
    public abstract boolean shouldInit();

    /**
     * Saves this PluginData instance to the database (asynchronously), to then be synced with other servers' repositories.
     *
     * @return Whether the data was saved and synced successfully.
     */
    public boolean saveAndSync() {
        return crescentCore.getPluginDBManager().asyncSaveData(this).exceptionally(throwable -> {
            crescentCore.getLogger().warning("Failed to save data: " + throwable.getMessage());
            return null;
        }).thenApplyAsync(pluginData -> {
            Sender send = crescentCore.getCrescentSunConnection().send(CrescentCore.PLUGIN_DATA_REGISTRY.getPluginDataSerializer().serialize(this));
            return send.isSuccess();
        }).join();
    }

    public boolean deleteAndSync() {
        // Async delete the data and handle errors
        PluginData pluginData = crescentCore.getPluginDataRepository().removeData(this.getClass(), getUuid());
        if (pluginData != null ) {
            return crescentCore.getPluginDBManager().asyncDeleteData(this.getClass(), getUuid()).exceptionally(throwable -> {
                crescentCore.getLogger().warning("Failed to delete data from database: " + throwable.getMessage());
                crescentCore.getPluginDataRepository().addDataInstance(this.getClass(), getUuid(), pluginData);
                return false;
            }).thenApplyAsync(success -> {
                Sender send = crescentCore.getCrescentSunConnection().send(CrescentCore.PLUGIN_DATA_REGISTRY.getPluginDataSerializer().serialize(new PluginDataIdentifier(getClass(), getUuid())));
                return send.isSuccess();
            }).join(); // Wait for the async operation to complete
        } else {
            crescentCore.getLogger().warning("Failed to delete data from repository.");
            return false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}