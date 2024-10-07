package it.crescentsun.crescentcore.data;

import com.google.common.collect.ImmutableList;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataIdentifier;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.crescentcore.CrescentCore;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The PluginDataRegistry works in tandem with the PluginDataRepository and PluginData classes to provide a way for
 * plugins to seamlessly store serializable data in a database. Only class types containing data you want to store in a database should be registered here.<br><br>
 *
 * To create a new class type supporting database serialization, it must extend {@link PluginData}. More information can be found in the PluginData class.<br>
 * Once the class is created, it must be registered in the PluginDataRegistry using the {@link #registerDataClass(JavaPlugin, Class)} method.
 * The PluginDataRegistry will then handle the rest of the serialization process.<br>
 * To avoid issues, the registry is frozen when the event {@link org.bukkit.event.server.ServerLoadEvent} is called.
 */
public final class PluginDataRegistry implements PluginDataRegistryService {

    private boolean isFrozen;
    private final List<Class<? extends PluginData>> registry = new CopyOnWriteArrayList<>();
    private final ObjectSerializer pluginDataSerializer = new ObjectSerializer();
    private final CrescentCore crescentCore;

    public PluginDataRegistry(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
        pluginDataSerializer.register(byte[].class);
        pluginDataSerializer.register(PluginDataIdentifier.class);
        pluginDataSerializer.register(ObjectObjectImmutablePair.class);
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
    @Override
    public void registerDataClass(JavaPlugin plugin, Class<? extends PluginData> dataClass) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The plugin data registry is frozen and cannot be modified.");
        }
        // Throw exception if dataClass doesn't extend PluginData
        if (!PluginData.class.isAssignableFrom(dataClass)) {
            throw new IllegalArgumentException("The data class must extend PluginData.");
        }
        registry.add(dataClass);
        crescentCore.getPluginDataManager().addDataTypeToCache(dataClass);
        pluginDataSerializer.register(dataClass);
        if (SingletonPluginData.class.isAssignableFrom(dataClass)) {
            try {
                SingletonPluginData pluginData = (SingletonPluginData) dataClass.getConstructor().newInstance();
                crescentCore.getPluginDataManager().insertData(pluginData.getUuid(), pluginData, false);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                CrescentCore.getInstance().getLogger().severe("Failed to instantiate singleton plugin data class " + dataClass.getName());
                e.printStackTrace();
            }
        }
        CrescentCore.getInstance().getLogger().info("Plugin data class registered for plugin " + plugin.getName() + ": " + dataClass.getName());
    }

    @Override
    public List<Class<? extends PluginData>> getRegistry() {
        return ImmutableList.copyOf(registry);
    }

    public ObjectSerializer getPluginDataSerializer() {
        return pluginDataSerializer;
    }

    public void freezeRegistry() {
        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Plugin Data Registry frozen! " +
                "No more plugin data can be registered.");
    }

    @Override
    public boolean isRegistryFrozen() {
        return isFrozen;
    }
}