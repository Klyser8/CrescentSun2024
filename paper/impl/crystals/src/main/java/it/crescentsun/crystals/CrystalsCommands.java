package it.crescentsun.crystals;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.api.crystals.event.GenerateCrystalsEvent;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.triumphcmd.bukkit.annotation.Permission;
import it.crescentsun.triumphcmd.core.annotations.Command;
import it.crescentsun.triumphcmd.core.annotations.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(value = "crystals", alias = "cr")
public class CrystalsCommands {

    private final Crystals plugin;
    public CrystalsCommands(Crystals plugin) {
        this.plugin = plugin;
    }

    @Permission("crescent.crystals")
    @Command
    public void defaultCommand(CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/crystals help\" for help."),
                "/crystals help");
        sender.sendMessage(text);
    }

    @Permission("crescent.crystals.spawn")
    @Command("spawn")
    public void spawnCommand(CommandSender sender, Integer amount, CrystalSpawnAnimation animation) {
        if (!(sender instanceof Player player)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(textComponent);
            return;
        }
        if (amount < animation.getMin() || amount > animation.getMax()) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT,
                    "The amount must be greater than " + animation.getMin() + " and less than " + animation.getMax() + " for this animation.");
            sender.sendMessage(textComponent);
            return;
        }
        plugin.getCrystalsService().spawnCrystals(player, amount, CrystalSource.COMMAND, animation, player.getLocation());
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've spawned " + amount + " Crystal(s) with the " + animation + " animation.");
        player.sendMessage(textComponent);
    }

    @Permission("crescent.crystals.add")
    @Command("add")
    public void addCommand(CommandSender sender, Integer amount, @Optional Player target) {
        if (amount <= 0) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "The amount must be greater than 0.");
            sender.sendMessage(textComponent);
            return;
        }
        CrystalSource source = CrystalSource.COMMAND;
        GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, source, target);
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }
        amount = event.getAmount();
        target = event.getPlayer();
        if (target == null) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a player.");
            sender.sendMessage(textComponent);
            return;
        }
        plugin.getCrystalsService().addCrystals(target, amount, source);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've added " + amount + " Crystal(s) to " + target.getName() + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }

    @Permission("crescent.crystals.remove")
    @Command("remove")
    public void removeCommand(CommandSender sender, Integer amount, @Optional Player target) {
        if (amount <= 0) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "The amount must be greater than 0.");
            sender.sendMessage(textComponent);
            return;
        }
        if (target == null) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a player.");
            sender.sendMessage(textComponent);
            return;
        }
        plugin.getCrystalsService().removeCrystals(target, amount, CrystalSource.COMMAND);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've removed " + amount + " Crystal(s) from " + target.getName() + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }

    @Permission("crescent.crystals.set")
    @Command("set")
    public void setCommand(CommandSender sender, Integer amount, @Optional Player target) {
        if (amount < 0) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "The amount must be greater than or equal to 0.");
            sender.sendMessage(textComponent);
            return;
        }
        if (target == null) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You must specify a player.");
            sender.sendMessage(textComponent);
            return;
        }
        PlayerData playerData = plugin.getPlayerDataService().getData(target);
        java.util.Optional<Integer> currentAmount = playerData.getDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        int addedAmount = amount - currentAmount.orElse(0);
        CrystalSource source = CrystalSource.COMMAND;
        if (addedAmount > 0) {
            GenerateCrystalsEvent event = new GenerateCrystalsEvent(addedAmount, source, target);
            event.callEvent();
            if (event.isCancelled()) {
                return;
            }
            addedAmount = event.getAmount();
            target = event.getPlayer();
        }
        if (target == null) {
            plugin.getLogger().warning("The player was set to null after the event was called, when trying to run the '/crystals set' command.");
            return;
        }
        plugin.getCrystalsService().setCrystals(target, currentAmount.orElse(0) + addedAmount, source);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've set " + target.getName() + "'s Crystal(s) to " + amount + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }
}
