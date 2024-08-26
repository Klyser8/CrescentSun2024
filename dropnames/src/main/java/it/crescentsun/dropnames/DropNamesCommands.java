package it.crescentsun.dropnames;

import it.crescentsun.crescentcore.cmd.bukkit.annotation.Permission;
import it.crescentsun.crescentcore.cmd.core.BaseCommand;
import it.crescentsun.crescentcore.cmd.core.annotation.Command;
import it.crescentsun.crescentcore.cmd.core.annotation.Default;
import it.crescentsun.crescentcore.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crescentmsg.MessageType;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

@Command(value = "dropnames", alias = "dn")
public class DropNamesCommands extends BaseCommand {

    private final DropNames plugin;
    public DropNamesCommands(DropNames plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.dropnames")
    public void defaultCommand(final CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INFO, "Type \"/dropnames help\" for help.", "/dropnames help");
        sender.sendMessage(text);
    }

    @SubCommand("reload")
    @Permission("crescent.dropnames.reload")
    public void reloadCommand(final CommandSender sender) {
        plugin.getDropNamesConfig().loadConfig();
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "Config reloaded.", "");
        sender.sendMessage(text);
    }

}
