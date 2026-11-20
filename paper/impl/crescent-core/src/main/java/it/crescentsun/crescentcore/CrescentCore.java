package it.crescentsun.crescentcore;

import com.google.common.collect.ImmutableList;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.PrematureAccessException;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.player.PlayerDataRegistryService;
import it.crescentsun.api.crescentcore.data.player.PlayerDataService;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.crescentcore.command.CrescentCoreCommands;
import it.crescentsun.crescentcore.data.PlayerDataRegistry;
import it.crescentsun.crescentcore.data.PluginDataRegistry;
import it.crescentsun.crescentcore.data.ServerStatistics;
import it.crescentsun.crescentcore.data.DatabaseManager;
import it.crescentsun.crescentcore.data.PlayerDataManager;
import it.crescentsun.crescentcore.data.PluginDataManager;
import it.crescentsun.crescentcore.data.PluginDataRegistry;
import it.crescentsun.crescentcore.data.ServerStatistics;
import it.crescentsun.crescentcore.event.AutoSaveEvent;
import it.crescentsun.crescentcore.lang.CrescentCoreLocalization;
import it.crescentsun.crescentcore.listener.CrescentCoreListener;
import it.crescentsun.crescentcore.listener.MiscListener;
import it.crescentsun.crescentcore.messaging.MessagingService;
import it.crescentsun.crescentcore.messaging.PluginDataChange;
import it.crescentsun.crescentcore.messaging.RedisMessagingService;
import it.crescentsun.triumphcmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.ApiStatus;

import java.sql.Timestamp;
import java.util.*;

/**
 * The main class of CrescentCore.
 *
 * IMPORTANT NOTE: This plugin's event listeners should have the lowest priority.
 * This is to ensure that this plugin's events are handled first.
 */
public class CrescentCore extends CrescentPlugin implements CrescentCoreAPI {
    public static final UUID STATISTICS_UUID =  UUID.fromString("962a383f-8504-4d51-b790-e1eaeb3b007f");
    private DatabaseManager dbManager = null;

    private MessagingService messagingService;

    private PlayerDataRegistryService playerDataRegistryService;
    private PluginDataRegistryService pluginDataRegistryService;
    private boolean dataSystemsReady;

    private static CrescentCore instance;

    private String serverName;
    private List<String> serverList = new ArrayList<>();
    private ServerStatistics statistics;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist
        serverName = resolveServerName();
        serverList = new ArrayList<>(List.of(serverName));

        CrescentCoreLocalization crescentCoreLocalization = new CrescentCoreLocalization();
        crescentCoreLocalization.registerEnglishTranslations();
        crescentCoreLocalization.registerItalianTranslations();

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new CrescentCoreListener(this), this);
        getServer().getPluginManager().registerEvents(new MiscListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrescentCoreCommands(this));
        establishDatabaseConnection();
        messagingService = new RedisMessagingService(this,
                getConfig().getString("redis.host", "localhost"),
                getConfig().getInt("redis.port", 6379),
                getConfig().getString("redis.password", ""),
                getConfig().getInt("redis.database", 0),
                getConfig().getString("redis.channel", "crescent:plugin-data"),
                getConfig().getString("redis.server_registry_key", "crescent:servers:online"));
        initServices();
        PluginData.setPluginDataService(pluginDataService);
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> refreshOnlineServers(), 0L, 200L);
    }

    @Override
    public void onPlayerDataRegister(PlayerDataRegistryService service) {
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_USERNAME, DataType.VARCHAR_16, true, "");
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_FIRST_LOGIN, DataType.TIMESTAMP, true, new Timestamp(System.currentTimeMillis()));
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_LAST_SEEN, DataType.TIMESTAMP, true, new Timestamp(System.currentTimeMillis()));
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_LAST_LOGIN, DataType.TIMESTAMP, true, new Timestamp(System.currentTimeMillis()));

        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_PLAY_TIME, DataType.UNSIGNED_BIG_INT, true, 0L);

    }

    @Override
    public void onPluginDataRegister(PluginDataRegistryService service) {
        service.registerDataClass(this, ServerStatistics.class);
    }

    @Override
    public void onDataLoad() {
        for (PluginData pluginData : getPluginDataManager().getAllData().values()) {
            if (pluginData.isProxyDependent()) {
                continue;
            }
            pluginData.tryInit();
        }

        setStatistics(getPluginDataManager().getData(ServerStatistics.class, CrescentCore.STATISTICS_UUID));
        getStatistics().setLastRestart(new Timestamp(System.currentTimeMillis()));

        scheduleAutoSave();                                      //Schedules the auto save task
        dataSystemsReady = true;
    }

    @Override
    public void onProxyConnected() {
        for (PluginData pluginData : getPluginDataManager().getAllData().values()) {
            if (pluginData.isProxyDependent()) {
                pluginData.tryInit();
            }
        }
    }

    @Override
    public void onAutoSave() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getPlayerDataManager().updatePlayerSessionData(player);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getScheduler().cancelTasks(this);               //Cancels all tasks
        if (messagingService != null) messagingService.close();
        if (dbManager != null) {
            if (dataSystemsReady) {
                dbManager.saveEverything();
            }
            dbManager.disconnect();
        }
    }

    @Override
    protected void initServices() {
        // Only available to Crescent Core
        playerDataRegistryService = new PlayerDataRegistry();
        pluginDataRegistryService  = new PluginDataRegistry(this);

        playerDataService = new PlayerDataManager(this);
        pluginDataService = new PluginDataManager(this);
        crescentCoreAPI = this;
        serviceManager.register(PlayerDataRegistryService.class, playerDataRegistryService, this, ServicePriority.Normal);
        serviceManager.register(PlayerDataService.class, playerDataService, this, ServicePriority.Normal);
        serviceManager.register(PluginDataRegistryService.class, pluginDataRegistryService, this, ServicePriority.Normal);
        serviceManager.register(PluginDataService.class, pluginDataService, this, ServicePriority.Normal);
        serviceManager.register(CrescentCoreAPI.class, crescentCoreAPI, this, ServicePriority.Normal);
    }

    private void establishDatabaseConnection() {
        if (dbManager != null) {
            throw new IllegalStateException("DatabaseManager has already been initialized.");
        }
        dbManager = new DatabaseManager(this,
                getConfig().getString("db.host"),
                getConfig().getInt("db.port"),
                getConfig().getString("db.db_name"),
                getConfig().getString("db.user"),
                getConfig().getString("db.password"),
                getConfig().getInt("db.max_pool_size")
        );
    }

    private void scheduleAutoSave() {
        int interval = getConfig().getInt("auto_save_interval");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            boolean called = new AutoSaveEvent().callEvent();
            if (!called) {
                return;
            }
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                getPlayerDataManager().asyncSaveAllData();
            }
        }, interval, interval);
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    /**
     * Gets the plugin instance.
     *
     * @return the plugin instance
     */
    public static CrescentCore getInstance() {
        return instance;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    private String resolveServerName() {
        String environmentName = System.getenv("SERVER_NAME");
        if (environmentName != null && !environmentName.isBlank()) {
            return environmentName.trim();
        }

        // Keep config fallback for local/non-container launches and existing deployments.
        String configuredName = getConfig().getString("server_name");
        if (configuredName != null && !configuredName.isBlank()) {
            getLogger().warning("SERVER_NAME is unset; using the legacy server_name config value.");
            return configuredName.trim();
        }

        throw new IllegalStateException("SERVER_NAME environment variable is required (or set server_name in config.yml outside Docker).");
    }

    @Override
    public List<String> getServerList() {
        return List.copyOf(serverList);
    }

    private void refreshOnlineServers() {
        messagingService.updateServerHeartbeat(serverName).whenComplete((servers, error) -> {
            if (error != null) {
                getLogger().warning("Could not refresh the Redis server registry: " + error.getMessage());
                return;
            }
            Bukkit.getScheduler().runTask(this, () -> setServerList(new ArrayList<>(servers)));
        });
    }

    @ApiStatus.Internal
    public void setServerList(List<String> serverList) {
        this.serverList = ImmutableList.copyOf(serverList);
    }

    @ApiStatus.Internal
    public void setServerName(String serverName) {
        serverName = serverName.startsWith("dev") ? serverName.substring(3) : serverName;
        this.serverName = serverName;
    }

    public MessagingService getMessagingService() {
        return messagingService;
    }

    public void receivePluginDataChange(PluginDataChange change) {
        Bukkit.getScheduler().runTask(this, () -> getPluginDataManager().applyRemoteChange(change));
    }

    public void setStatistics(ServerStatistics statistics) {
        if (this.statistics != null) {
            getLogger().warning("Attempted to set a new ServerStatistics, but one is already set.");
        }
        this.statistics = statistics;
    }

    public ServerStatistics getStatistics() {
        if (statistics == null) {
            throw new PrematureAccessException("Statistics have not been initialized yet! Initialization takes place in the ServerLoadPostDBEvent of crescent-core!");
        }
        return statistics;
    }

    public PlayerDataManager getPlayerDataManager() {
        if (playerDataService == null) {
            throw new PrematureAccessException("PlayerDataManager has not been initialized yet!");
        }
        return (PlayerDataManager) playerDataService;
    }

    public PluginDataManager getPluginDataManager() {
        if (pluginDataService == null) {
            throw new PrematureAccessException("PluginDataManager has not been initialized yet!");
        }
        return (PluginDataManager) pluginDataService;
    }

    public PluginDataRegistry getPluginDataRegistry() {
        if (pluginDataRegistryService == null) {
            throw new PrematureAccessException("PluginDataRegistry has not been initialized yet!");
        }
        return (PluginDataRegistry) pluginDataRegistryService;
    }

    public PlayerDataRegistry getPlayerDataRegistry() {
        if (playerDataRegistryService == null) {
            throw new PrematureAccessException("PlayerDataRegistry has not been initialized yet!");
        }
        return (PlayerDataRegistry) playerDataRegistryService;
    }
}
