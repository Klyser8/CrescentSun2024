package it.crescentsun.api.crescentcore.util;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import org.bukkit.Bukkit;

public class TableNameUtil {

    /**
     * Generates the table name for player data of a specific plugin.
     *
     * @param pluginName The name of the plugin.
     * @return The table name in the format "player_data_<plugin_name>".
     */
    public static String appendPlayerDataTablePrefix(String pluginName) {
        if (pluginName.startsWith("player_data_")) {
            Bukkit.getLogger().warning("Plugin name " + pluginName + " already starts with 'player_data_'!");
            new Exception().printStackTrace();
            return pluginName;
        }
        return "player_data_" + pluginName.toLowerCase();
    }

    /**
     * Generates the table name for plugin data based on the data class.
     *
     * @param dataClass The class of the plugin data.
     * @return The table name in the format "<plugin_name>_<data_class_name>".
     */
    public static String appendPluginDataTablePrefix(Class<? extends PluginData> dataClass) {
        if (!dataClass.isAnnotationPresent(DatabaseTable.class)) {
            throw new IllegalArgumentException("Plugin data class " + dataClass.getName() + " does not have a DatabaseTable annotation!");
        }
        DatabaseTable annotation = dataClass.getAnnotation(DatabaseTable.class);
        String pluginName = annotation.plugin().getSimpleName().toLowerCase().replace(" ", "_");
        String tableName = annotation.tableName().toLowerCase();
        return pluginName + "_" + tableName;
    }

    /**
     * Checks if a table name is considered a player data table.
     *
     * @param tableName The name of the table.
     * @return True if the table name is a player data table, false otherwise.
     */
    public static boolean isPlayerDataTable(String tableName) {
        return tableName.startsWith("player_data_");
    }

    /**
     * Extracts the plugin name from a player data table name.
     *
     * @param tableName The raw table name.
     * @return The plugin name.
     */
    public static String extractPluginNameFromPlayerDataTable(String tableName) {
        if (!isPlayerDataTable(tableName)) {
            throw new IllegalArgumentException("Table name " + tableName + " is not a player data table!");
        }
        return tableName.substring("player_data_".length());
    }
}