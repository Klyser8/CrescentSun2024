package it.crescentsun.crescentcore.api.data.plugin;

import com.google.common.collect.ImmutableList;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * The PluginDataRegistry works in tandem with the PluginDataRepository and PluginData classes to provide a way for
 * plugins to seamlessly store serializable data in a database. Only class types containing data you want to store in a database should be registered here.<br><br>
 *
 * To create a new class type supporting database serialization, it must extend {@link PluginData}. More information can be found in the PluginData class.<br>
 * Once the class is created, it must be registered in the PluginDataRegistry using the {@link #registerDataClass(JavaPlugin, Class)} method.
 * The PluginDataRegistry will then handle the rest of the serialization process.<br>
 * To avoid issues, the registry is frozen when the event {@link org.bukkit.event.server.ServerLoadEvent} is called.
 */
public final class PluginDataRegistry {

    private final PluginDataRepository dataRepository;
    private static boolean isFrozen;
    private final List<Class<? extends PluginData>> registry = new ArrayList<>();

    public PluginDataRegistry() {
        dataRepository = new PluginDataRepository();
    }

    /**
     * Registers a new PluginData class.
     * Once registered, the PluginDataRegistry will handle the serialization and deserialization of the data.
     *
     * @param plugin The plugin registering the data class
     * @param dataClass The class type to register. Must extend PluginData.
     * @throws UnsupportedOperationException If the registry is frozen and cannot be modified.
     * @throws IllegalArgumentException If the data class provided does not extend PluginData.
     */
    public void registerDataClass(JavaPlugin plugin, Class<? extends PluginData> dataClass) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The plugin data registry is frozen and cannot be modified.");
        }
        // Throw exception if dataClass doesn't extend PluginData
        if (dataClass.getSuperclass() != null && dataClass.getSuperclass().isInstance(PluginData.class)) {
            throw new IllegalArgumentException("The data class must extend PluginData.");
        }
        registry.add(dataClass);
        dataRepository.registerNew(dataClass);
        CrescentCore.getInstance().getLogger().info("Plugin data class registered for plugin " + plugin.getName() + ": " + dataClass.getName());
    }

    @ApiStatus.Internal
    public static void freezeRegistries() {
        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Plugin Data Registry frozen! " +
                "No more plugin data can be registered.");
    }

    public List<Class<? extends PluginData>> getRegistry() {
        return ImmutableList.copyOf(registry);
    }

    public PluginDataRepository getDataRepository() {
        return dataRepository;
    }

}
