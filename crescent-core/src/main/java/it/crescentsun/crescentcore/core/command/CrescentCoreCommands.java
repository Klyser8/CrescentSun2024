package it.crescentsun.crescentcore.core.command;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.BungeeUtils;
import it.crescentsun.crescentcore.core.data.player.PlayerData;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crescentmsg.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("ALL")
@Command(value = "crescent", alias = "cs")
public class CrescentCoreCommands extends BaseCommand {

    private final CrescentCore plugin;

    public CrescentCoreCommands(CrescentCore plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.crescentcore")
    public void defaultCommand(final CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/crescent help\" for help."), "/crescent help");
        sender.sendMessage(text);
    }

    @SubCommand("switch")
    @Permission("crescent.crescentcore.switch")
    public void switchCommand(final CommandSender sender, String serverName) {
        if (!(sender instanceof Player player)) {
            TextComponent text = MessageFormatter.formatCommandMessage(
                    MessageType.INCORRECT, Component.text("Only players can use this command."));
            sender.sendMessage(text);
            return;
        }
        if (serverName == null || serverName.isEmpty()) {
            TextComponent text = MessageFormatter.formatCommandMessage(
                    MessageType.INCORRECT, Component.text("You must specify a server name."));
            sender.sendMessage(text);
            return;
        }
        CompletableFuture<Boolean> boolFut = BungeeUtils.saveDataAndSendPlayerToServer(
                plugin, plugin, player, serverName);
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("You will be teleported once your data is saved. Please wait..."));
        sender.sendMessage(text);
    }

    @SubCommand("save")
    @Permission("crescent.crescentcore.save")
    public void saveCommand(final CommandSender sender) {
        sender.sendMessage(MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Saving all plugin data...")));
        CompletableFuture<Map<UUID, PlayerData>> futureSaveMap = plugin.getPlayerManager().asyncSaveAllData();
        futureSaveMap.thenAccept(saveMap -> {
            if (!saveMap.isEmpty()) {
                TextComponent text = MessageFormatter.formatCommandMessage(
                        MessageType.SUCCESS, Component.text("All plugin data has been saved!"));
                sender.sendMessage(text);
            } else {
                TextComponent text = MessageFormatter.formatCommandMessage(
                        MessageType.ERROR, Component.text("Data could not be saved - Something's wrong."));
                sender.sendMessage(text);
            }
        });
    }
}
