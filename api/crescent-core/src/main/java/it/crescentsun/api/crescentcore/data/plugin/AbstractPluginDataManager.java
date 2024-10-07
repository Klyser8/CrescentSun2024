package it.crescentsun.api.crescentcore.data.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class for managing plugin data. Any class you want to be managing your implementations of PluginData should extend this class.
 *
 * @param <J> Instance of the plugin that is managing the data.
 * @param <P> The type of PluginData that is being managed.
 */
public abstract class AbstractPluginDataManager<J extends JavaPlugin, P extends PluginData> {

    protected final J plugin;
    protected final Class<P> pluginDataType;
    private final PluginDataService pluginDataService;

    public AbstractPluginDataManager(J plugin, Class<P> pluginDataType, PluginDataService pluginDataService) {
        this.plugin = plugin;
        this.pluginDataType = pluginDataType;
        this.pluginDataService = pluginDataService;
    }

    /**
     * Returns all PluginData instances tied to the PluginData type being managed.
     *
     * @param isInitialized Whether to return only initialized data instances.
     * @return A list of PluginData instances.
     */
    public List<P> getAllData(boolean isInitialized) {
        ArrayList<P> allData;
        if (isInitialized) {
            allData = new ArrayList<>();
            for (P data : pluginDataService.getAllDataOfType(pluginDataType).values()) {
                if (data.initialized) {
                    allData.add(data);
                }
            }
        } else {
            allData =  new ArrayList<>(pluginDataService.getAllDataOfType(pluginDataType).values());
        }
        return allData;
    }

    /**
     * Fetches the respective PluginData instance from the repository, based on the UUID provided.
     * @param uuid The UUID of the data instance.
     * @return The PluginData instance.
     */
    public P getDataInstance(UUID uuid) {
        return pluginDataService.getData(pluginDataType, uuid);
    }

}
