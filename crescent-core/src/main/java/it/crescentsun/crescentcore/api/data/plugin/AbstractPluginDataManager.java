package it.crescentsun.crescentcore.api.data.plugin;

import it.crescentsun.crescentcore.CrescentCore;
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
    protected final CrescentCore crescentCore;
    protected final PluginDataRepository dataRepository;

    public AbstractPluginDataManager(J plugin, Class<P> pluginDataType) {
        this.plugin = plugin;
        this.pluginDataType = pluginDataType;
        crescentCore = CrescentCore.getInstance();
        dataRepository = crescentCore.getPluginDataRepository();
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
            for (P data : dataRepository.getAllDataOfType(pluginDataType).values()) {
                if (data.initialized) {
                    allData.add(data);
                }
            }
        } else {
            allData =  new ArrayList<>(dataRepository.getAllDataOfType(pluginDataType).values());
        }
        return allData;
    }

    public P getDataInstance(UUID uuid) {
        return dataRepository.getData(pluginDataType, uuid);
    }

}
