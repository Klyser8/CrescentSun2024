package it.crescentsun.crescentcore.data;

import com.google.common.collect.ImmutableList;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataIdentifier;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.util.TableNameUtil;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
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
    private final CrescentCore crescentCore;

    public PluginDataRegistry(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
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
        Objects.requireNonNull(plugin, "Registering plugin cannot be null");
        Objects.requireNonNull(dataClass, "Plugin data class cannot be null");
        // Throw exception if dataClass doesn't extend PluginData
        if (!PluginData.class.isAssignableFrom(dataClass)) {
            throw new IllegalArgumentException("The data class must extend PluginData.");
        }
        if (registry.contains(dataClass)) {
            throw new IllegalArgumentException("Plugin data class registered more than once: " + dataClass.getName());
        }
        if (!dataClass.isAnnotationPresent(DatabaseTable.class)) {
            throw new IllegalArgumentException("Plugin data class " + dataClass.getName()
                    + " must be annotated with @DatabaseTable.");
        }
        Class<?> declaredOwner = dataClass.getAnnotation(DatabaseTable.class).plugin();
        if (!declaredOwner.isInstance(plugin)) {
            throw new IllegalArgumentException("Plugin data class " + dataClass.getName() + " declares owner "
                    + declaredOwner.getName() + " but was registered by " + plugin.getClass().getName() + ".");
        }
        registry.add(dataClass);
        CrescentCore.getInstance().getLogger().info("Plugin data class registered for plugin " + plugin.getName() + ": " + dataClass.getName());
    }

    @Override
    public List<Class<? extends PluginData>> getRegistry() {
        return ImmutableList.copyOf(registry);
    }

    public void freezeRegistry() {
        if (isFrozen) return;

        List<String> playerTableNames = new ArrayList<>();
        for (String pluginName : crescentCore.getPlayerDataRegistry().getRegisteredPluginNames()) {
            playerTableNames.add(TableNameUtil.appendPlayerDataTablePrefix(pluginName));
        }
        List<Class<? extends PluginData>> registeredClasses = List.copyOf(registry);
        crescentCore.getPluginDataManager().initializeRegisteredDataTypes(registeredClasses, playerTableNames);

        for (Class<? extends PluginData> dataClass : registeredClasses) {
            if (!SingletonPluginData.class.isAssignableFrom(dataClass)) continue;
            try {
                var constructor = dataClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                SingletonPluginData pluginData = (SingletonPluginData) constructor.newInstance();
                crescentCore.getPluginDataManager().insertData(pluginData.getUuid(), pluginData, false);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException exception) {
                throw new IllegalStateException("Failed to instantiate singleton plugin data class "
                        + dataClass.getName() + " during registry freeze.", exception);
            }
        }

        isFrozen = true;
        CrescentCore.getInstance().getLogger().info("Plugin Data Registry frozen! " +
                "No more plugin data can be registered.");
    }

    @Override
    public boolean isRegistryFrozen() {
        return isFrozen;
    }
}
