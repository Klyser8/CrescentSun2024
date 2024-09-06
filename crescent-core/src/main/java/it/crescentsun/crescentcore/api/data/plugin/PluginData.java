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


    protected PluginData() {
        // Check that any subclass is annotated with @DatabaseTable
        if (!this.getClass().isAnnotationPresent(DatabaseTable.class)) { //TODO test
            throw new IllegalStateException("Plugin data class " + this.getClass().getName() + " should be annotated with @DatabaseTable!");
        }
    }

    public abstract UUID getUuid();

    /**
     * Initializes the data instance. This method should be overridden in the subclass to perform any additional necessary initialization. <br>
     * <b>IMPORTANT: Only ever call this method for instances that are to be used in the current server, as this method should be used to
     * initialize elements that are server-specific.</b>
     */
    public void init() {
        if (crescentCore.getPluginDataRepository().getData(this.getClass(), getUuid()) == null) {
            crescentCore.getPluginDataRepository().addDataInstance(this.getClass(), getUuid(), this);
        }
    }

    /**
     * Saves this PluginData instance to the database (asynchronously), to then be synced with other servers' repositories.
     *
     * @return Whether the data was saved and synced successfully.
     */
    public boolean saveAndSync() {
        // Async save data
        return crescentCore.getPluginDataManager().asyncSaveData(this).exceptionally(throwable -> {
            crescentCore.getLogger().warning("Failed to save data: " + throwable.getMessage());
            return null;
        }).join() != null;
        /*Sender send = crescentCore.getCrescentSunConnection().send(crescentCore.getObjectSerializer().serialize(this));
        return send.isSuccess();*/
    }

    public boolean delete() {
        // Async delete the data and handle errors
        return crescentCore.getPluginDataManager().asyncDeleteData(this.getClass(), getUuid()).exceptionally(throwable -> {
            crescentCore.getLogger().warning("Failed to delete data: " + throwable.getMessage());
            return false;
        }).thenApplyAsync(success -> {
            // Only proceed to remove data from the repository if the deletion was successful
            if (success) {
                PluginData pluginData = crescentCore.getPluginDataRepository().removeData(this.getClass(), getUuid());
                if (pluginData == null) {
                    crescentCore.getLogger().warning("Failed to remove data from repository: " + getUuid());
                    return false;
                }
                return true;
            } else {
                return false; // Deletion failed, no need to remove from repository
            }
        }).join(); // Wait for the async operation to complete
    }

}