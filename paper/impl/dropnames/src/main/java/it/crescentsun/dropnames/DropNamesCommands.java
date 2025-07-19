package it.crescentsun.dropnames;

import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.triumphcmd.bukkit.annotation.Permission;
import it.crescentsun.triumphcmd.core.annotations.Command;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

@Command(value = "dropnames", alias = "dn")
public class DropNamesCommands {

    private final DropNames plugin;
    public DropNamesCommands(DropNames plugin) {
        this.plugin = plugin;
    }

    @Permission("crescent.dropnames")
    @Command
    public void defaultCommand(final CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INFO, "Type \"/dropnames help\" for help.", "/dropnames help");
        sender.sendMessage(text);
    }

}
