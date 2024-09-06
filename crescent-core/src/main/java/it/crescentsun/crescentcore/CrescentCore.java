package it.crescentsun.crescentcore;

import it.crescentsun.crescentcore.api.CrystalsProvider;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.core.BungeeConstants;
import it.crescentsun.crescentcore.core.command.CrescentCoreCommands;
import it.crescentsun.crescentcore.core.db.DatabaseManager;
import it.crescentsun.crescentcore.core.db.PlayerDBManager;
import it.crescentsun.crescentcore.core.db.PluginDBManager;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import it.crescentsun.crescentcore.core.listener.BungeeListener;
import it.crescentsun.crescentcore.core.listener.CrescentCoreListener;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.player.PlayerDataRegistry;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import it.crescentsun.crescentcore.core.protoweaver.CrescentSunClientHandler;
import it.crescentsun.crescentcore.core.protoweaver.CrescentSunServerHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;
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

    private DatabaseManager dbManager = null;

    private Protocol crescentSunProtocol = Protocol.create("crescentsun", "msg")
            .setClientHandler(CrescentSunServerHandler.class)
            .setServerHandler(CrescentSunClientHandler.class)
            .setMaxPacketSize(67108864)
            .setCompression(CompressionType.GZIP)
            .addPacket(Byte[].class)
            .load();
    private ProtoConnection crescentSunConnection;
    private ObjectSerializer objectSerializer = new ObjectSerializer();

    private final Random random;
    public static final PlayerDataRegistry PLAYER_DATA_ENTRY_REGISTRY = new PlayerDataRegistry();
    public static final PluginDataRegistry PLUGIN_DATA_REGISTRY = new PluginDataRegistry();
    private PluginDataRepository pluginDataRepository;
    private static CrescentCore instance;
    private CrystalsProvider crystalsProvider = null;
    private int debugLevel = 0;
    private String serverName;
    public CrescentCore() {
        random = new Random();
    }

    @Override
    public void onEnable() {
        instance = this;
        objectSerializer.register(Byte[].class);
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist

        ProtoWeaver.load(crescentSunProtocol);
        debugLevel = getConfig().getInt("debug_level");           //Reads the debug level from the config
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

        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_USERNAME, DataType.VARCHAR_16, "");
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_FIRST_LOGIN, DataType.TIMESTAMP, new Timestamp(0));
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_LAST_SEEN, DataType.TIMESTAMP, new Timestamp(0));
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

    public PluginDBManager getPluginDataManager() {
        return dbManager.getPluginDataManager();
    }

    public PlayerDBManager getPlayerDataManager() {
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

    public int getDebugLevel() {
        return debugLevel;
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

    @ApiStatus.Internal
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Protocol getCrescentSunProtocol() {
        return crescentSunProtocol;
    }

    public ProtoConnection getCrescentSunConnection() {
        return crescentSunConnection;
    }

    public void setCrescentSunConnection(ProtoConnection crescentSunConnection) {
        this.crescentSunConnection = crescentSunConnection;
    }

    public ObjectSerializer getObjectSerializer() {
        return objectSerializer;
    }
}
