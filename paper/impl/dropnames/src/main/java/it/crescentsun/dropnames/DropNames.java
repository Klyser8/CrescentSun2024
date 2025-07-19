package it.crescentsun.dropnames;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.triumphcmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import javax.management.ServiceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DropNames extends CrescentPlugin {

    private DropNamesConfig dropNamesConfig;
    public static final UUID CONFIG_UUID = UUID.fromString("dd712431-3914-48af-ba14-4916daa150a9");

    @Override
    public void onEnable() {
        initServices();
        Bukkit.getPluginManager().registerEvents(new ItemDropListener(this), this);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new DropNamesCommands(this));
    }

    @Override
    public void onPluginDataRegister(PluginDataRegistryService service) {
        service.registerDataClass(this, DropNamesConfig.class);
    }

    @Override
    public void onDataLoad() {
        dropNamesConfig = pluginDataService.getData(DropNamesConfig.class, CONFIG_UUID);
    }

    public DropNamesConfig getDropNamesConfig() {
        return dropNamesConfig;
    }

    @Override
    protected void initServices() {
        try {
            pluginDataService = getServiceProvider(PluginDataService.class);
        } catch (ServiceNotFoundException e) {
            getLogger().severe("Service initialization failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}
