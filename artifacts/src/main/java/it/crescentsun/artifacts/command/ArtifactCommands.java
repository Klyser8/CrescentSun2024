package it.crescentsun.artifacts.command;

import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.AsyncArtifact;
import it.crescentsun.crescentcore.api.InventoryUtils;
import it.crescentsun.crescentcore.api.crystals.event.CrystalSource;
import it.crescentsun.crescentcore.api.crystals.event.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.api.registry.ArtifactNamespaceKeys;
import it.crescentsun.crescentcore.cmd.bukkit.annotation.Permission;
import it.crescentsun.crescentcore.cmd.core.BaseCommand;
import it.crescentsun.crescentcore.cmd.core.annotation.Command;
import it.crescentsun.crescentcore.cmd.core.annotation.Default;
import it.crescentsun.crescentcore.cmd.core.annotation.Optional;
import it.crescentsun.crescentcore.cmd.core.annotation.SubCommand;
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
        if (artifact.namespacedKey().equals(ArtifactNamespaceKeys.CRYSTAL)) {
            GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, CrystalSource.COMMAND, player);
            event.callEvent();
            if (event.isCancelled()) {
                return;
            }
            amount = event.getAmount();
            player = event.getPlayer();
        }
        int finalAmount = amount;
        Player finalPlayer = player;
        if (artifact instanceof AsyncArtifact asyncArtifact) {
            asyncArtifact.getItemAsync(finalAmount).thenAccept(stack -> {
                giveItemAndNotify(sender, finalPlayer, artifact, finalAmount, stack);
            });
        } else {
            ItemStack stack = artifact.getItem(finalAmount);
            giveItemAndNotify(sender, player, artifact, finalAmount, stack);
        }
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
