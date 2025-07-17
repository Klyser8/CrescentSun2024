package it.crescentsun.artifacts.command;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.crescentcore.util.InventoryUtils;
import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(value = "artifacts", alias = "art")
public class ArtifactCommands extends BaseCommand {

    private final Artifacts plugin;

    public ArtifactCommands(Artifacts plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.artifacts")
    public void defaultCommand(CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/artifacts help\" for help."),
                "/artifacts help");
        sender.sendMessage(text);
    }

    @SubCommand("give")
    @Permission("crescent.artifacts.give")
    public void giveCommand(CommandSender sender, Player player, Artifact artifact, @Optional Integer amount) { //TODO test
        if (artifact == null) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Artifact not found.", "");
            sender.sendMessage(textComponent);
            return;
        }
        if (amount == null) {
            amount = 1;
        }
        if (amount < 0) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Amount must be greater than 0.", "0");
            sender.sendMessage(textComponent);
            return;
        }
        if (amount == 0) {
            amount = 1;
        }
        if (artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
            //TODO add new flag to prevent the item from being able to be given with commands
        }
        int finalAmount = amount;
        ItemStack stack = artifact.createStack(finalAmount);
        giveItemAndNotify(sender, player, artifact, finalAmount, stack);
    }

    private void giveItemAndNotify(CommandSender sender, Player player, Artifact artifact, int finalAmount, ItemStack stack) {
        if (InventoryUtils.isInventoryFull(player, stack)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Player's inventory is full.", "full");
            sender.sendMessage(textComponent);
            return;
        }
        player.getInventory().addItem(stack);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've given " + player.getName()
                + " " + finalAmount + " " + artifact.namespacedKey().getKey() + "s.");
        sender.sendMessage(textComponent);
        textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've received " + finalAmount + " "
                + artifact.namespacedKey().getKey() + "s from " + sender.getName() + ".");
        player.sendMessage(textComponent);
    }


}
