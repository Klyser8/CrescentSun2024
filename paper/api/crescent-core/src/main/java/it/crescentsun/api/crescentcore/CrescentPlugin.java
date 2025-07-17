package it.crescentsun.api.crescentcore;

import it.crescentsun.api.crescentcore.data.player.PlayerDataRegistryService;
import it.crescentsun.api.crescentcore.data.player.PlayerDataService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.api.crescentcore.event.server.ProxyConnectedEvent;
import it.crescentsun.api.crescentcore.event.server.DataLoadedEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.management.ServiceNotFoundException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * A plugin that uses CrescentCore's data management services.
 * Extend this class to use the services provided by CrescentCore.
 * You should populate the fields in this class with the respective services, and create getters as fit.
 */
public abstract class CrescentPlugin extends JavaPlugin {

    protected ServicesManager serviceManager = getServer().getServicesManager();
    protected final Random random = new Random();

    protected CrescentCoreAPI crescentCoreAPI;
    protected PlayerDataService playerDataService;
    protected PluginDataService pluginDataService;
    private static Logger logger;
    private static String pluginName;

    public CrescentPlugin() {
        logger = getLogger();
        pluginName = getName();
    }

    /**
     * Called when all data has been cached from the database onto memory.
     * This always happens after all plugins have finished loading, in the {@link ServerLoadEvent}.<br><br>
     * Here any code reliant on the data being loaded should be executed, though you can also listen to the {@link DataLoadedEvent} <br>
     * <b>Any logic which other plugins may rely on should NOT be run async in this method.</b>
     */
    public void onDataLoad() {

    }

    /**
     * Called when the plugin is ready to register new player data entries.
     *
     * @see PlayerDataRegistryService
     */
    public void onPlayerDataRegister(PlayerDataRegistryService service) {

    }

    /**
     * Called when the plugin's no longer able to register new player data entries.
     */
    public void onPlayerDataRegistryFreeze() {

    }

    /**
     * Called when the plugin is ready to register new plugin data classes.
     *
     * @see PluginDataRegistryService
     */
    public void onPluginDataRegister(PluginDataRegistryService service) {

    }

    /**
     * Called when the plugin has finished registering plugin data classes.
     *
     * @see DataLoadedEvent
     */
    public void onPluginDataRegistryFreeze() {

    }

    /**
     * Called when the plugin has successfully connected to the (velocity) proxy, through ProtoWeaver.
     * This is always called after the database data has been cached, usually 1-5 seconds after the backend server
     * has finished loading.<br><br>
     * Any code reliant on other backend servers or the proxy should be executed here, though you can also listen to the {@link ProxyConnectedEvent} <br>
     * <b>Any logic which other plugins may rely on should NOT be run async in this method.</b>
     */
    public void onProxyConnected() {

    }

    /**
     * Called when Crescent Core is about to save all plugin & player data, on the auto-save interval.<br>
     */
    public void onAutoSave() {

    }

    /**
     * Extend to expand on the services being initialized or fetched, then call in the onEnable().
     * <b>Do NOT call supermethod if being extended in Crescent Core!</b>
     */
    protected void initServices() {
        try {
            playerDataService = getServiceProvider(PlayerDataService.class);
            pluginDataService = getServiceProvider(PluginDataService.class);
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the specified service provider.
     * Should be called in the initServices method.
     *
     * @param serviceClass The class of the service provider.
     * @return The service provider.
     * @param <T> The type of the service provider.
     * @throws ServiceNotFoundException If the service provider is not found.
     */
    protected <T> T getServiceProvider(Class<T> serviceClass) throws ServiceNotFoundException {
        RegisteredServiceProvider<T> rsp = serviceManager.getRegistration(serviceClass);
        if (rsp == null) {
            throw new ServiceNotFoundException(serviceClass.getSimpleName() + " not found");
        }
        return rsp.getProvider();
    }

    /**
     * Gets the instance of the Random object defined above.
     * @return the Random object.
     */
    public Random random() {
        if (random == null) {
            throw new IllegalStateException("Random object not initialized");
        }
        return random;
    }

    public CrescentCoreAPI getCrescentCoreAPI() {
        if (crescentCoreAPI == null) {
            throw new IllegalStateException("CrescentCoreAPI service not initialized");
        }
        return crescentCoreAPI;
    }

    public PlayerDataService getPlayerDataService() {
        if (playerDataService == null) {
            throw new IllegalStateException("PlayerDataService not initialized");
        }
        return playerDataService;
    }

    public PluginDataService getPluginDataService() {
        if (pluginDataService == null) {
            throw new IllegalStateException("PluginDataService not initialized");
        }
        return pluginDataService;
    }

    /**
     * Creates a namespaced key with the given key.
     * The namespace will always be the plugin's name (as defined in the plugin.yml).
     *
     * @param key The key to create the namespaced key with.
     * @return The namespaced key.
     */
    public static NamespacedKey id(String key) {
        return new NamespacedKey(name(), key);
    }

    @NotNull
    public static Logger logger() {
        return logger;
    }

    @NotNull
    public static String name() {
        return pluginName;
    }
}
