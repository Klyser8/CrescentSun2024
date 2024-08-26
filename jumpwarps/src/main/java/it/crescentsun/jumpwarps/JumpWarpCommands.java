package it.crescentsun.jumpwarps;

import it.crescentsun.crescentcore.cmd.bukkit.annotation.Permission;
import it.crescentsun.crescentcore.cmd.core.BaseCommand;
import it.crescentsun.crescentcore.cmd.core.annotation.Command;
import it.crescentsun.crescentcore.cmd.core.annotation.Default;
import it.crescentsun.crescentcore.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crescentmsg.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(value = "jumpwarps", alias = "jw")
public class JumpWarpCommands extends BaseCommand {

    private final JumpWarps plugin;

    public JumpWarpCommands(JumpWarps plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.jumpwarps")
    public void defaultCommand(final CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/jumpwarps help\" for help."), "/jumpwarps help");
        sender.sendMessage(text);
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
        if (plugin.getJumpWarpManager().createJumpWarp(player, warpName, targetServerName)) {
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
        if (!plugin.getJumpWarpManager().getJumpWarps().containsKey(warpName)) {
            TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INCORRECT,
                    "JumpWarp \"" + warpName + "\" does not exist.", warpName);
            sender.sendMessage(text);
            return;
        }
        plugin.getJumpWarpManager().deleteJumpWarp(warpName);
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.SUCCESS,
                "JumpWarp \"" + warpName + "\" deleted successfully.", warpName);
        sender.sendMessage(text);
    }

    @SubCommand("list")
    @Permission("crescent.jumpwarps.list")
    public void listCommand(final CommandSender sender) {
        //Iterate through each key in JumpWarp, then send a message showing the JumpWarp name, location, and target server name.
        TextComponent text = MessageFormatter.formatCommandMessage(MessageType.INFO, "List of JumpWarps:");
        sender.sendMessage(text);
        plugin.getJumpWarpManager().getJumpWarps().forEach((warpName, jumpWarp) -> {
            TextComponent warpText = MessageFormatter.formatCommandMessage(MessageType.INFO,
                    warpName +
                            " (" + jumpWarp.getX() + " " + jumpWarp.getY() + " " + jumpWarp.getZ()
                            + ") - Target Server: " + jumpWarp.getTargetServerName(),
                    warpName,
                    String.valueOf(jumpWarp.getX()), String.valueOf(jumpWarp.getY()), String.valueOf(jumpWarp.getZ()),
                    jumpWarp.getTargetServerName());
            sender.sendMessage(warpText);
        });
    }

}
