package it.crescentsun.crystals;

import it.crescentsun.crescentcore.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.crescentcore.api.crystals.event.CrystalSource;
import it.crescentsun.crescentcore.api.crystals.event.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.api.registry.CrescentNamespacedKeys;
import it.crescentsun.crescentcore.api.util.PlayerUtils;
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

@Command(value = "crystals", alias = "cr")
public class CrystalsCommands extends BaseCommand {

    private final Crystals plugin;
    public CrystalsCommands(Crystals plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("crescent.crystals")
    public void defaultCommand(CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/crystals help\" for help."),
                "/crystals help");
        sender.sendMessage(text);
    }

    @SubCommand("spawn")
    @Permission("crescent.crystals.spawn")
    public void spawnCommand(CommandSender sender, @Optional Integer amount) {//max int:
        if (!(sender instanceof Player)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(textComponent);
        }
        if (amount <= 0) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "The amount must be greater than 0.");
            sender.sendMessage(textComponent);
        }
        if (sender instanceof Player player) {
            plugin.spawnCrystals(player, amount, CrystalSource.COMMAND, CrystalSpawnAnimation.HOVER);
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've spawned " + amount + " Crystal(s).");
            player.sendMessage(textComponent);
        }
    }

    @SubCommand("add")
    @Permission("crescent.crystals.add")
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
        plugin.addCrystals(target, amount, source);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've added " + amount + " Crystal(s) to " + target.getName() + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }

    @SubCommand("remove")
    @Permission("crescent.crystals.remove")
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
        plugin.removeCrystals(target, amount, CrystalSource.COMMAND);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've removed " + amount + " Crystal(s) from " + target.getName() + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }

    @SubCommand("set")
    @Permission("crescent.crystals.set")
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
        PlayerData playerData = PlayerUtils.getPlayerData(target);
        int currentAmount = playerData.getDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        int addedAmount = amount - currentAmount;
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
        plugin.setCrystals(target, currentAmount + addedAmount, source);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've set " + target.getName() + "'s Crystal(s) to " + amount + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }
}
