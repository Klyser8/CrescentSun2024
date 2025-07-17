package it.crescentsun.api.crescentcore.data.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a database table.
 * This annotation should be used on classes that extend {@link PluginData}.
 * The name given to the table will automatically have the prefix [plugin_name]_ attached to it.
 * E.G.: If the plugin is named "Crystals", and tableName is "settings", the final name will be "crystals_settings".
 *
 * @see PluginData
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatabaseTable {

    /**
     * Specifies the name of the table in the database.
     *
     * @return The name of the table.
     */
    String tableName();

    /**
     * Specifies the plugin class associated with the table.
     *
     * @return The class of the plugin.
     */
    Class<? extends JavaPlugin> plugin();
}