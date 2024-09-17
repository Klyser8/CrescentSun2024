package it.crescentsun.crescentcore;

import com.google.common.collect.ImmutableList;
import it.crescentsun.crescentcore.api.ArtifactsProvider;
import it.crescentsun.crescentcore.api.CrystalsProvider;
import it.crescentsun.crescentcore.api.PrematureAccessException;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.crescentsun.crescentcore.api.registry.CrescentNamespacedKeys;
import it.crescentsun.crescentcore.core.ServerStatistics;
import it.crescentsun.crescentcore.core.network.BungeeConstants;
import it.crescentsun.crescentcore.core.command.CrescentCoreCommands;
import it.crescentsun.crescentcore.core.db.DatabaseManager;
import it.crescentsun.crescentcore.core.db.PlayerDataManager;
import it.crescentsun.crescentcore.core.db.PluginDataManager;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import it.crescentsun.crescentcore.core.listener.BungeeListener;
import it.crescentsun.crescentcore.core.listener.CrescentCoreListener;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.player.PlayerDataRegistry;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import it.crescentsun.crescentcore.core.network.CrescentSunServerHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.sql.Timestamp;
import java.util.*;

/**
 * The main class of CrescentCore.
 *
 * IMPORTANT NOTE: This plugin's event listeners should have the lowest priority.
 * This is to ensure that this plugin's events are handled first.
 */
public class CrescentCore extends JavaPlugin {
    public static final UUID STATISTICS_UUID =  UUID.fromString("962a383f-8504-4d51-b790-e1eaeb3b007f");
    private DatabaseManager dbManager = null;

    private Protocol crescentSunProtocol = Protocol.create("crescentsun", "msg")
            .setServerHandler(CrescentSunServerHandler.class)
            .setMaxPacketSize(67108864)
            .setCompression(CompressionType.GZIP)
            .addPacket(byte[].class)
            .load();
    private ProtoConnection crescentSunConnection;

    private final Random random;
    public static final PlayerDataRegistry PLAYER_DATA_ENTRY_REGISTRY = new PlayerDataRegistry();
    public static final PluginDataRegistry PLUGIN_DATA_REGISTRY = new PluginDataRegistry();
    private PluginDataRepository pluginDataRepository;

    private static CrescentCore instance;
    private CrystalsProvider crystalsProvider = null;
    private ArtifactsProvider artifactsProvider = null;

    private String serverName;
    private List<String> serverList = new ArrayList<>();
    private ServerStatistics statistics;
    public CrescentCore() {
        random = new Random();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist

        pluginDataRepository = PLUGIN_DATA_REGISTRY.getDataRepository();
        scheduleAutoSave();                                      //Schedules the auto save task

        CrescentCoreLocalization crescentCoreLocalization = new CrescentCoreLocalization();
        crescentCoreLocalization.registerEnglishTranslations();
        crescentCoreLocalization.registerItalianTranslations();

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new CrescentCoreListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeConstants.MESSAGING_CHANNEL_BUNGEE);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeConstants.MESSAGING_CHANNEL_CRESCENTSUN_DB);
        getServer().getMessenger().registerIncomingPluginChannel(this, BungeeConstants.MESSAGING_CHANNEL_BUNGEE, new BungeeListener(this));
        getServer().getMessenger().registerIncomingPluginChannel(this, BungeeConstants.MESSAGING_CHANNEL_CRESCENTSUN_DB, new BungeeListener(this));
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrescentCoreCommands(this));
        establishDatabaseConnection();

        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespacedKeys.PLAYER_USERNAME, DataType.VARCHAR_16, "");
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespacedKeys.PLAYER_FIRST_LOGIN, DataType.TIMESTAMP, new Timestamp(0));
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespacedKeys.PLAYER_LAST_SEEN, DataType.TIMESTAMP, new Timestamp(0));

        PLUGIN_DATA_REGISTRY.registerDataClass(this, ServerStatistics.class);
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
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                getPlayerDataManager().asyncSaveAllData();
            }
        }, interval, interval);
    }

    public void setCrystalsProvider(CrystalsProvider provider) {
        if (crystalsProvider != null) {
            getLogger().warning("Attempted to set a new CrystalsProvider, but one is already set.");
        }
        crystalsProvider = provider;
    }

    public CrystalsProvider getCrystalsProvider() {
        return crystalsProvider;
    }

    public void setArtifactsProvider(ArtifactsProvider provider) {
        if (artifactsProvider != null) {
            getLogger().warning("Attempted to set a new ArtifactsProvider, but one is already set.");
        }
        artifactsProvider = provider;
    }

    public ArtifactsProvider getArtifactsProvider() {
        return artifactsProvider;
    }

    public PluginDataManager getPluginDataManager() {
        return dbManager.getPluginDataManager();
    }

    public PlayerDataManager getPlayerDataManager() {
        return dbManager.getPlayerDataManager();
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

    public Random getRandom() {
        return random;
    }

    public PluginDataRepository getPluginDataRepository() {
        return pluginDataRepository;
    }

    public String getServerName() {
        return serverName;
    }

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
}
