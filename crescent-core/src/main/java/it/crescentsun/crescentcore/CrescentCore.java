package it.crescentsun.crescentcore;

import com.google.common.collect.ImmutableList;
import it.crescentsun.crescentcore.api.CrystalsProvider;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.core.network.BungeeConstants;
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
    private String serverName;
    private List<String> serverList = new ArrayList<>();
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
                getPlayerDBManager().asyncSaveAllData();
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

    public PluginDBManager getPluginDBManager() {
        return dbManager.getPluginDataManager();
    }

    public PlayerDBManager getPlayerDBManager() {
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
}
