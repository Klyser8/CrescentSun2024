package it.crescentsun.api.crescentcore.data.plugin;

import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * The PluginDataRegistryService works in tandem with the PluginData classes to provide a way for
 * plugins to seamlessly store serializable data in a database. Only class types containing data you want to store in a database should be registered here.<br><br>
 *
 * To create a new class type supporting database serialization, it must extend {@link PluginData}. More information can be found in the PluginData class.<br>
 * Once the class is created, it must be registered in the PluginDataRegistry using the {@link #registerDataClass(JavaPlugin, Class)} method.
 * The PluginDataRegistryService will then handle the rest of the serialization process.<br>
 * To avoid issues, the registry is frozen when the event {@link ServerLoadEvent} is called.
 */
public interface PluginDataRegistryService {

    /**
     * Registers a new PluginData class.
     * Once registered, the PluginDataRegistry will handle the serialization and deserialization of the data.
     *
     * @param plugin The plugin registering the data class
     * @param dataClass The class type to register. Must extend PluginData.
     */
    void registerDataClass(JavaPlugin plugin, Class<? extends PluginData> dataClass);

    /**
     * Returns the list of registered PluginData classes.
     * @return An immutable copy of the list of registered PluginData classes
     */
    List<Class<? extends PluginData>> getRegistry();

    /**
     * Returns whether the registry is frozen and can no longer be modified.
     * If an attempt to modify the registry is made after it has been frozen, an UnsupportedOperationException will be thrown.
     * @return Whether the registry is frozen
     */
    boolean isRegistryFrozen();


}
