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
import it.crescentsun.crescentcore.network.CrescentSunServerHandler;
import it.crescentsun.triumphcmd.bukkit.BukkitCommandManager;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
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

    private Protocol crescentSunProtocol = Protocol.create("crescentsun", "msg")
            .setServerHandler(CrescentSunServerHandler.class)
            .setMaxPacketSize(67108864)
            .setCompression(CompressionType.GZIP)
            .addPacket(byte[].class)
            .load();
    private ProtoConnection crescentSunConnection;

    private PlayerDataRegistryService playerDataRegistryService;
    private PluginDataRegistryService pluginDataRegistryService;

    private static CrescentCore instance;

    private String serverName;
    private List<String> serverList = new ArrayList<>();
    private ServerStatistics statistics;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist

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
        initServices();
        PluginData.setPluginDataService(pluginDataService);
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
        dbManager.saveEverything();
        if (dbManager != null) {
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

    @Override
    public List<String> getServerList() {
        return List.copyOf(serverList);
    }

    @ApiStatus.Internal
    public void setServerList(List<String> serverList) {
        // Copy to a mutable list
        serverList = new ArrayList<>(serverList);
        serverList.replaceAll(s -> s.startsWith("dev") ? s.substring(3) : s);
        this.serverList = ImmutableList.copyOf(serverList);
    }

    @ApiStatus.Internal
    public void setServerName(String serverName) {
        serverName = serverName.startsWith("dev") ? serverName.substring(3) : serverName;
        this.serverName = serverName;
    }

    public ProtoConnection getCrescentSunConnection() {
        return crescentSunConnection;
    }

    public void setCrescentSunConnection(ProtoConnection crescentSunConnection) {
        this.crescentSunConnection = crescentSunConnection;
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
