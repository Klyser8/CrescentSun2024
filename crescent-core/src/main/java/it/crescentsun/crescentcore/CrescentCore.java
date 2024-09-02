package it.crescentsun.crescentcore;

import it.crescentsun.crescentcore.api.CrystalsProvider;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.core.command.CrescentCoreCommands;
import it.crescentsun.crescentcore.core.data.JumpWarp;
import it.crescentsun.crescentcore.core.data.ServerStats;
import it.crescentsun.crescentcore.core.db.DatabaseManager;
import it.crescentsun.crescentcore.core.db.PlayerDBManager;
import it.crescentsun.crescentcore.core.db.PluginDBManager;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import it.crescentsun.crescentcore.core.listener.BungeeListener;
import it.crescentsun.crescentcore.core.listener.CrescentCoreListener;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.player.PlayerDataRegistry;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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
    private final Random random;
    public static final PlayerDataRegistry PLAYER_DATA_ENTRY_REGISTRY = new PlayerDataRegistry();
    public static final PluginDataRegistry PLUGIN_DATA_REGISTRY = new PluginDataRegistry();
//    public static final PluginDataRegistry PLUGIN_DATA_ENTRY_REGISTRY = new PluginDataRegistry();
    public static final List<Class<?>> REGISTERED_PLUGIN_DATA_CLASSES = new ArrayList<>();
    private static CrescentCore instance;
    private CrystalsProvider crystalsProvider = null;
    private int debugLevel = 0;
    public static final int LIVE_PORT = 25565;
    public static final int TEST_PORT = 25564;
    public CrescentCore() {
        random = new Random();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist
        debugLevel = getConfig().getInt("debug_level");           //Reads the debug level from the config
        scheduleAutoSave();                                      //Schedules the auto save task

        CrescentCoreLocalization crescentCoreLocalization = new CrescentCoreLocalization();
        crescentCoreLocalization.registerEnglishTranslations();
        crescentCoreLocalization.registerItalianTranslations();

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new CrescentCoreListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener());
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrescentCoreCommands(this));
        establishDatabaseConnection();

        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_USERNAME, DataType.VARCHAR_16, "");
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_FIRST_LOGIN, DataType.TIMESTAMP, new Timestamp(0));
        PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespaceKeys.PLAYER_LAST_SEEN, DataType.TIMESTAMP, new Timestamp(0));

        PLUGIN_DATA_REGISTRY.registerDataClass(this, ServerStats.class);
        PLUGIN_DATA_REGISTRY.registerDataClass(this, JumpWarp.class);
/*        PLUGIN_DATA_ENTRY_REGISTRY.registerPluginDataBundle(CrescentNamespaceKeys.CRESCENTCORE_SERVER_STATS,
                PluginDataBundle.Builder.create(this, "server", DataType.VARCHAR_16, "server")
                        .addUnsignedInt("total_players")
                        .addTimestamp("last_restart")
                        .addUnsignedInt("most_players_online")
                        .build());*/
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

}
