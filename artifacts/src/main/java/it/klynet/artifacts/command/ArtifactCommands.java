package it.klynet.artifacts.command;

import it.klynet.adventurousklynetmsg.MessageFormatter;
import it.klynet.adventurousklynetmsg.MessageType;
import it.klynet.artifacts.Artifacts;
import it.klynet.artifacts.item.Artifact;
import it.klynet.artifacts.item.AsyncArtifact;
import it.klynet.klynetcore.api.InventoryUtils;
import it.klynet.klynetcore.api.event.crystals.CrystalGenerationSource;
import it.klynet.klynetcore.api.event.crystals.GenerateCrystalsEvent;
import it.klynet.klynetcore.api.registry.ArtifactNamespaceKeys;
import it.klynet.klynetcore.cmd.bukkit.annotation.Permission;
import it.klynet.klynetcore.cmd.core.BaseCommand;
import it.klynet.klynetcore.cmd.core.annotation.Command;
import it.klynet.klynetcore.cmd.core.annotation.Default;
import it.klynet.klynetcore.cmd.core.annotation.Optional;
import it.klynet.klynetcore.cmd.core.annotation.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static it.klynet.klynetcore.api.CommandSenderUtil.sendFormattedMessage;

@Command(value = "artifacts", alias = "art")
public class ArtifactCommands extends BaseCommand {

    private final Artifacts plugin;

    public ArtifactCommands(Artifacts plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("klynet.artifacts")
    public void defaultCommand(CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/artifacts help\" for help."),
                "/artifacts help");
        sender.sendMessage(text);
    }

    @SubCommand("give")
    @Permission("klynet.artifacts.give")
    public void giveCommand(CommandSender sender, Player player, Artifact artifact, @Optional Integer amount) { //TODO test
        if (artifact == null) {
            MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Artifact not found.", "");
            return;
        }
        if (amount == null) {
            amount = 1;
        }
        if (amount < 0) {
            sendFormattedMessage(sender, MessageType.INCORRECT, "Amount must be greater than 0.");
            return;
        }
        if (amount == 0) {
            amount = 1;
        }
        if (artifact.namespacedKey().equals(ArtifactNamespaceKeys.CRYSTAL)) {
            GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, CrystalGenerationSource.COMMAND, player);
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
            sendFormattedMessage(sender, MessageType.INCORRECT, "Player's inventory is full.");
            return;
        }
        player.getInventory().addItem(stack);
        sendFormattedMessage(sender, MessageType.SUCCESS, "You've given " + player.getName()
                + " " + finalAmount + " " + artifact.namespacedKey().getKey() + "s.");
        sendFormattedMessage(player, MessageType.SUCCESS, "You've received " + finalAmount + " "
                + artifact.namespacedKey().getKey() + "s from " + sender.getName() + ".");
    }


}
