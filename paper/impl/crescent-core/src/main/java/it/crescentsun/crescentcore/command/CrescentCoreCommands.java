package it.crescentsun.crescentcore.command;

import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.BungeeUtils;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.lang.CrescentCoreLocalization;
import it.crescentsun.triumphcmd.bukkit.annotation.Permission;
import it.crescentsun.triumphcmd.core.annotations.Command;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//@SuppressWarnings("ALL")
@Command(value = "crescent", alias = "cs")
public class CrescentCoreCommands {

    private final CrescentCore crescentCore;

    public CrescentCoreCommands(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @Command
    @Permission("crescent.crescentcore")
    public void defaultCommand(final CommandSender sender) {
        if (sender instanceof Player player) {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_INCORRECT_COMMAND.getFormattedMessage(player.locale(), "/crescent help"));
        } else {
            sender.sendMessage(CrescentCoreLocalization.GENERIC_INCORRECT_COMMAND.getFormattedMessage(null, "/crescent help"));
        }
    }

    @Command("switch")
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
        crescentCore.getPlayerDataManager().saveDataAsync(player.getUniqueId()).whenComplete((playerData, error) ->
                crescentCore.getServer().getScheduler().runTask(crescentCore, () -> {
                    if (error == null && playerData != null && player.isOnline()) {
                        BungeeUtils.sendPlayerToServer(crescentCore, player, serverName);
                    } else {
                        sender.sendMessage(CrescentCoreLocalization.GENERIC_TELEPORTATION_FAILURE.getFormattedMessage(player.locale(), serverName));
                    }
                }));

        sender.sendMessage(CrescentCoreLocalization.GENERIC_AWAIT_TELEPORTATION.getFormattedMessage(player.locale(), serverName));
        // If player is still connected to this server five seconds later, send error message
        crescentCore.getServer().getScheduler().runTaskLater(crescentCore, () -> {
            if (player.isOnline()) {
                sender.sendMessage(CrescentCoreLocalization.GENERIC_TELEPORTATION_FAILURE.getFormattedMessage(player.locale(), serverName));
            }
        }, 100L); // 5 seconds
    }

    @Command("save")
    @Permission("crescent.crescentcore.save")
    public void saveCommand(final CommandSender sender) {
        sender.sendMessage(CrescentCoreLocalization.SAVING_PLAYER_DATA.getFormattedMessage(null));
        CompletableFuture<Map<UUID, PlayerData>> futurePlayerMap = crescentCore.getPlayerDataManager().asyncSaveAllData();
        futurePlayerMap.whenComplete((saveMap, error) -> crescentCore.getServer().getScheduler().runTask(crescentCore, () -> {
            boolean success = error == null && saveMap != null;
            TextComponent text = (TextComponent) (success
                    ? CrescentCoreLocalization.SAVING_PLAYER_DATA_SUCCESS.getFormattedMessage(null)
                    : CrescentCoreLocalization.SAVING_PLAYER_DATA_FAILURE.getFormattedMessage(null));
            sender.sendMessage(text);
        }));
    }

    @Command("reload")
    @Permission("crescent.crescentcore.reload")
    public void reloadCommand(final CommandSender sender) {
        crescentCore.getPluginDataManager().asyncReloadAllData().whenComplete((success, error) ->
                crescentCore.getServer().getScheduler().runTask(crescentCore, () -> {
                    if (error == null && Boolean.TRUE.equals(success)) {
                        sender.sendMessage(CrescentCoreLocalization.RELOADING_DATA_SUCCESS.getFormattedMessage(null));
                    } else {
                        sender.sendMessage(CrescentCoreLocalization.RELOADING_DATA_FAILURE.getFormattedMessage(null));
                    }
                }));
    }
}
