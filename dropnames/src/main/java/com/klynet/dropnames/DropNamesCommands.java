package com.klynet.dropnames;

import it.klynet.adventurousklynetmsg.MessageFormatter;
import it.klynet.adventurousklynetmsg.MessageType;
import it.klynet.klynetcore.cmd.bukkit.annotation.Permission;
import it.klynet.klynetcore.cmd.core.BaseCommand;
import it.klynet.klynetcore.cmd.core.annotation.Command;
import it.klynet.klynetcore.cmd.core.annotation.Default;
import it.klynet.klynetcore.cmd.core.annotation.SubCommand;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

@Command(value = "dropnames", alias = "dn")
public class DropNamesCommands extends BaseCommand {

    private final DropNames plugin;
    public DropNamesCommands(DropNames plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("klynet.dropnames")
    public void defaultCommand(final CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INFO, "Type \"/dropnames help\" for help.", "/dropnames help");
        sender.sendMessage(text);
    }

    @SubCommand("reload")
    @Permission("klynet.dropnames.reload")
    public void reloadCommand(final CommandSender sender) {
        plugin.getDropNamesConfig().loadConfig();
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "Config reloaded.", "");
        sender.sendMessage(text);
    }

}
