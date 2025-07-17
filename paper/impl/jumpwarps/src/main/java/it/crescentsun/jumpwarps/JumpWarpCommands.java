package it.crescentsun.jumpwarps;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.jumpwarps.lang.JumpWarpLocalization;
import it.crescentsun.jumpwarps.warphandling.JumpWarpData;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

@Command(value = "jumpwarps", alias = "jw")
public class JumpWarpCommands extends BaseCommand {

    private final JumpWarps plugin;

    public JumpWarpCommands(JumpWarps plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.jumpwarps")
    public void defaultCommand(final CommandSender sender) {
        if (sender instanceof Player player) {
            player.sendMessage(JumpWarpLocalization.UNKNOWN_USAGE.getFormattedMessage(player.locale()));
        } else {
            sender.sendMessage(JumpWarpLocalization.UNKNOWN_USAGE.getFormattedMessage(null));
        }
    }

    @SubCommand("help")
    @Permission("crescent.jumpwarps.help")
    public void helpCommand(final CommandSender sender) {
        TextComponent text1 = MessageFormatter.formatCommandMessage(MessageType.INFO,
                "[JumpWarps Help] (/jw help)", "[JumpWarps Help] (/jw help)");
        TextComponent text2 = MessageFormatter.formatCommandMessage(MessageType.INFO,
                "\"/jumpwarps create <warp_name> <target_server_name>\": creates a new JumpWarp at the player's location.",
                "/jumpwarps create <warp_name> <target_server_name>");
        TextComponent text3 = MessageFormatter.formatCommandMessage(MessageType.INFO,
                "\"/jumpwarps delete <warp_name>\": deletes the specified JumpWarp.",
                "/jumpwarps delete <warp_name>");
        sender.sendMessage(text1);
        sender.sendMessage(text2);
        sender.sendMessage(text3);
    }

    @SubCommand("create")
    @Permission("crescent.jumpwarps.create")
    public void createCommand(final CommandSender sender, final String warpName, final String targetServerName) {
        if (!(sender instanceof Player player)) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(text);
            return;
        }
        if (warpName == null || warpName.isEmpty()) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a JumpWarp name.");
            sender.sendMessage(textComponent);
            return;
        }
        if (targetServerName == null || targetServerName.isEmpty()) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a target server name.");
            sender.sendMessage(text);
            return;
        }
        if (plugin.getJumpWarpManager().createJumpWarp(player, warpName, targetServerName) != null) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.SUCCESS,
                    "JumpWarp \"" + warpName + "\" created successfully!", warpName);
            sender.sendMessage(text);
        } else {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Failed to create JumpWarp.");
            sender.sendMessage(text);
        }
    }

    @SubCommand("delete")
    @Permission("crescent.jumpwarps.delete")
    public void deleteCommand(final CommandSender sender, final String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a JumpWarp name.");
            sender.sendMessage(text);
            return;
        }
        if (!plugin.getJumpWarpManager().deleteJumpWarp(warpName)) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT,
                    "JumpWarp \"" + warpName + "\" does not exist.", warpName);
            sender.sendMessage(text);
            return;
        }
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.SUCCESS,
                "JumpWarp \"" + warpName + "\" deleted successfully.", warpName);
        sender.sendMessage(text);
    }

    @SubCommand("list")
    @Permission("crescent.jumpwarps.list")
    public void listCommand(final CommandSender sender, @Optional final Boolean networkWide) {
        Locale locale = sender instanceof Player player ? player.locale() : null;
        if (networkWide == null || !networkWide) {
            List<JumpWarpData> jumpWarps = plugin.getJumpWarpManager().getAllData(true);
            sender.sendMessage(JumpWarpLocalization.LIST_TITLE_SERVER.getFormattedMessage(locale, plugin.getCrescentCoreAPI().getServerName()));
            sender.sendMessage(JumpWarpLocalization.LIST_HEADER_SERVER.getFormattedMessage(locale));
            for (JumpWarpData jumpWarp : jumpWarps) {
                sender.sendMessage(JumpWarpLocalization.LIST_ROW_SERVER.getFormattedMessage(locale,
                        jumpWarp.getWarpName(),
                        jumpWarp.getLocation().getWorld() == null ? "unknown" : jumpWarp.getLocation().getWorld().getName(),
                        String.valueOf(jumpWarp.getX()), String.valueOf(jumpWarp.getY()), String.valueOf(jumpWarp.getZ()),
                        jumpWarp.getDestinationServer()
                ));
            }
        } else {
            List<JumpWarpData> jumpWarps = plugin.getJumpWarpManager().getAllData(false);
            sender.sendMessage(JumpWarpLocalization.LIST_TITLE_NETWORK.getFormattedMessage(locale));
            sender.sendMessage(JumpWarpLocalization.LIST_HEADER_NETWORK.getFormattedMessage(locale));
            for (JumpWarpData jumpWarp : jumpWarps) {
                sender.sendMessage(JumpWarpLocalization.LIST_ROW_NETWORK.getFormattedMessage(locale,
                        jumpWarp.getWarpName(),
                        jumpWarp.getServer(),
                        jumpWarp.getLocation().getWorld() == null ? "unknown" : jumpWarp.getLocation().getWorld().getName(),
                        String.valueOf(jumpWarp.getX()), String.valueOf(jumpWarp.getY()), String.valueOf(jumpWarp.getZ()),
                        jumpWarp.getDestinationServer()
                ));
            }
        }
    }

}
