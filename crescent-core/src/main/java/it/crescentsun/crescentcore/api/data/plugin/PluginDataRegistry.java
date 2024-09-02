package it.crescentsun.crescentcore.api.data.plugin;

import com.google.common.collect.ImmutableList;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.core.data.PluginDataRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PluginDataRegistry {

    private final PluginDataRepository dataRepository;
    private static boolean isFrozen;
    private final List<Class<? extends PluginData>> registry = new ArrayList<>();

    public PluginDataRegistry() {
        dataRepository = new PluginDataRepository();
    }

    public void registerDataClass(JavaPlugin plugin, Class<? extends PluginData> dataClass) {
        if (isFrozen) {
            throw new UnsupportedOperationException("The plugin data registry is frozen and cannot be modified.");
        }
        registry.add(dataClass);
        dataRepository.registerNew(dataClass);
        CrescentCore.getInstance().getLogger().info("Plugin data class registered for plugin " + plugin.getName() + ": " + dataClass.getName());
    }

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
