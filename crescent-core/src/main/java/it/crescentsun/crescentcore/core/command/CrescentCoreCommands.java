package it.crescentsun.crescentcore.core.command;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.BungeeUtils;
import it.crescentsun.crescentcore.api.data.player.PlayerData;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//@SuppressWarnings("ALL")
@Command(value = "crescent", alias = "cs")
public class CrescentCoreCommands extends BaseCommand {

    private final CrescentCore crescentCore;

    public CrescentCoreCommands(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @Default
    @Permission("crescent.crescentcore")
    public void defaultCommand(final CommandSender sender) {
        if (sender instanceof Player player) {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_INCORRECT_COMMAND.getFormattedMessage(player.locale(), "/crescent help"));
        } else {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_INCORRECT_COMMAND.getFormattedMessage(null, "/crescent help"));
        }
    }

    @SubCommand("switch")
    @Permission("crescent.crescentcore.switch")
    public void switchCommand(final CommandSender sender, String serverName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_CONSOLE_INVALID.getFormattedMessage(null));
            return;
        }
        if (serverName == null || serverName.isEmpty()) {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_SPECIFY_SERVER_NAME.getFormattedMessage(player.locale()));
            return;
        }
        if (serverName.equalsIgnoreCase(crescentCore.getServerName())) {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_ALREADY_CONNECTED_TO_SERVER.getFormattedMessage(player.locale(), serverName));
            return;
        }
        crescentCore.getPlayerDBManager().asyncSaveData(player.getUniqueId()).thenApplyAsync(playerData -> {
            if (playerData != null) {
                BungeeUtils.sendPlayerToServer(
                        crescentCore, player, serverName);
                return playerData;
            } else {
                sender.sendMessage(CrescentCoreLocalization.GENERIC_TELEPORTATION_FAILURE.getFormattedMessage(player.locale(), serverName));
                return null;
            }
        });

        sender.sendMessage(CrescentCoreLocalization.GENERIC_AWAIT_TELEPORTATION.getFormattedMessage(player.locale(), serverName));
    }

    @SubCommand("save")
    @Permission("crescent.crescentcore.save")
    public void saveCommand(final CommandSender sender) {
        sender.sendMessage(CrescentCoreLocalization.SAVING_PLAYER_DATA.getFormattedMessage(null));
        CompletableFuture<Map<UUID, PlayerData>> futurePlayerMap = crescentCore.getPlayerDBManager().asyncSaveAllData();
        futurePlayerMap.thenAccept(saveMap -> {
            TextComponent text;
            if (!saveMap.isEmpty()) {
                text = (TextComponent) CrescentCoreLocalization.SAVING_PLAYER_DATA_SUCCESS.getFormattedMessage(null);
            } else {
                text = (TextComponent) CrescentCoreLocalization.SAVING_PLAYER_DATA_FAILURE.getFormattedMessage(null);
            }
            sender.sendMessage(text);
        });
    }

    @SubCommand("reload")
    @Permission("crescent.crescentcore.reload")
    public void reloadCommand(final CommandSender sender) {
        boolean success = crescentCore.getPluginDBManager().reloadAllData();
        if (success) {
            sender.sendMessage(CrescentCoreLocalization.RELOADING_DATA_SUCCESS.getFormattedMessage(null));
        } else {
            sender.sendMessage(CrescentCoreLocalization.RELOADING_DATA_FAILURE.getFormattedMessage(null));
        }
    }
}
