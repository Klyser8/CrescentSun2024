package it.klynet.klynetcore;

import it.klynet.klynetcore.api.CrystalsProvider;
import it.klynet.klynetcore.plugindata.DataType;
import it.klynet.klynetcore.plugindata.PluginDataRegistry;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import it.klynet.klynetcore.core.command.KlyNetCommands;
import it.klynet.klynetcore.core.data.player.PlayerDataManager;
import it.klynet.klynetcore.core.data.DatabaseManager;
import it.klynet.klynetcore.core.listener.KlyNetCoreListener;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.util.*;

/**
 * The main class of KlyNetCore.
 *
 * IMPORTANT NOTE: This plugin's event listeners should have the lowest priority.
 * This is to ensure that this plugin's events are handled first.
 */
public class KlyNetCore extends JavaPlugin {

    private DatabaseManager dbManager = null;
    private final Random random;
    public static final PluginDataRegistry PLAYER_DATA_REGISTRY = new PluginDataRegistry();
    private static KlyNetCore instance;
    private CrystalsProvider crystalsProvider = null;
    private int debugLevel = 0;
    public static final int LIVE_LOBBY_PORT = 8880;
    public static final int TEST_LOBBY_PORT = 9880;
    public KlyNetCore() {
        random = new Random();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();                                     //Saves the default config.yml file if it doesn't exist
        debugLevel = getConfig().getInt("debug_level");           //Reads the debug level from the config
        scheduleAutoSave();                                      //Schedules the auto save task

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new KlyNetCoreListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new KlyNetCommands(this));
        establishDatabaseConnection();

        PLAYER_DATA_REGISTRY.registerPluginData(KlyNetNamespaceKeys.PLAYER_USERNAME, DataType.VARCHAR_16, "");
        PLAYER_DATA_REGISTRY.registerPluginData(KlyNetNamespaceKeys.PLAYER_FIRST_LOGIN, DataType.TIMESTAMP, new Timestamp(0));
        PLAYER_DATA_REGISTRY.registerPluginData(KlyNetNamespaceKeys.PLAYER_LAST_SEEN, DataType.TIMESTAMP, new Timestamp(0));
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
                getPlayerManager().asyncSaveAllData();
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

    public PlayerDataManager getPlayerManager() {
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
    public static KlyNetCore getInstance() {
        return instance;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public Random getRandom() {
        return random;
    }

}
