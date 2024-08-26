package it.crescentsun.dropnames;

import it.crescentsun.crescentcore.cmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class DropNames extends JavaPlugin {

    private DropNamesConfig dropNamesConfig;

    @Override
    public void onEnable() {
        dropNamesConfig = new DropNamesConfig(this);
        dropNamesConfig.loadConfig();
        Bukkit.getPluginManager().registerEvents(new ItemDropListener(this), this);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new DropNamesCommands(this));
    }

    @Override
    public void onDisable() {
    }

    public DropNamesConfig getDropNamesConfig() {
        return dropNamesConfig;
    }

}
