package it.crescentsun.dropnames;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
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

}
