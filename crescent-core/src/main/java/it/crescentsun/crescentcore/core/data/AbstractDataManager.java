package it.crescentsun.crescentcore.core.data;

import it.crescentsun.crescentcore.CrescentCore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A generic data manager class that handles getting, updating,
 * loading and saving data for a given data holder.
 *
 * @param <K> the dataHolder key (e.g. UUID for players)
 * @param <V> the dataHolder (e.g. PlayerData)
 */
public abstract class AbstractDataManager<K, V> {

    protected final Map<K, V> dataMap = new HashMap<>();

    /**
     * The plugin instance.
     */
    protected final CrescentCore crescentCore;

    /**
     * The database manager instance.
     */
    protected final DatabaseManager dbManager;

    /**
     * Constructs a new data manager instance.
     *
     * @param crescentCore the plugin instance
     */
    protected AbstractDataManager(CrescentCore crescentCore, DatabaseManager dbManager) {
        this.crescentCore = crescentCore;
        this.dbManager = dbManager;
    }

    /**
     * Gets the data for the given data holder.
     *
     * @param dataKey the dataKey
     * @return the data, or null if not found
     */
    public V getData(K dataKey) {
        return this.dataMap.get(dataKey);
    }

    /**
     * Sets the data for the given data holder.
     *
     * @param dataKey the dataKey
     * @param dataHolder the dataHolder
     */
    public void setData(K dataKey, V dataHolder) {
        this.dataMap.put(dataKey, dataHolder);
    }

    /**
     * Checks if the given data holder has data.
     *
     * @param dataKey the dataKey
     * @return true if the data holder has data, false otherwise
     */
    public boolean hasData(K dataKey) {
        return this.dataMap.containsKey(dataKey);
    }

    /**
     * Removes the data for the given data holder.
     *
     * @param dataKey the dataKey
     */
    public void removeData(K dataKey) {
        this.dataMap.remove(dataKey);
    }

    /**
     * Clears all data.
     */
    public void clearData() {
        this.dataMap.clear();
    }

    /**
     * Saves the data for the given data holder to the database.
     *
     * @param dataKey the dataKey
     * @return the saved data
     */
    public abstract V saveData(K dataKey);

    /**
     * Loads the data for the given data holder from the database.
     *
     * @param dataKey the dataKey
     * @return the loaded data, or null if not found
     */
    public abstract V loadData(K dataKey);

    /**
     * Creates default data for the given data holder, if none exists.
     *
     * @param dataKey the dataKey
     * @return the default data
     */
    public V setDefaultData(K dataKey) {
        return null;
    }

    /**
     * Saves all data to the database.
     */
    public abstract Map<K, V> saveAllData();

    public abstract CompletableFuture<Boolean> loadAllData();

}
