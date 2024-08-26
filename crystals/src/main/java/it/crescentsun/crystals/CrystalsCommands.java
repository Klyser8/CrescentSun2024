package it.crescentsun.crystals;

import it.crescentsun.crescentcore.api.PlayerUtils;
import it.crescentsun.crescentcore.api.event.crystals.CrystalGenerationSource;
import it.crescentsun.crescentcore.api.event.crystals.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentcore.cmd.bukkit.annotation.Permission;
import it.crescentsun.crescentcore.cmd.core.BaseCommand;
import it.crescentsun.crescentcore.cmd.core.annotation.Command;
import it.crescentsun.crescentcore.cmd.core.annotation.Default;
import it.crescentsun.crescentcore.cmd.core.annotation.Optional;
import it.crescentsun.crescentcore.cmd.core.annotation.SubCommand;
import it.crescentsun.crescentcore.core.data.player.PlayerData;
import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crescentmsg.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys.SETTINGS_SHOW_CRYSTALIX;

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

    @SubCommand("toggle")
    @Permission("crescent.crystals.toggle")
    public void toggleCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(textComponent);
            return;
        }
        PlayerData playerData = plugin.getCrescentCore().getPlayerManager().getData(player.getUniqueId());
        boolean isShowing = playerData.getData(SETTINGS_SHOW_CRYSTALIX);
        playerData.updateData(SETTINGS_SHOW_CRYSTALIX, !isShowing);
        isShowing = !isShowing;
        TextComponent textComponent;
        if (isShowing) {
            plugin.getCrystalixManager().createCrystalix(player);
            textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've toggled your Crystalix ON.", "ON");
        } else {
            plugin.getCrystalixManager().removeCrystalix(player);
            textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've toggled your Crystalix OFF.", "OFF");
        }
        player.sendMessage(textComponent);
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
            plugin.spawnCrystals(player, amount, CrystalGenerationSource.COMMAND);
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
        GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, CrystalGenerationSource.COMMAND, target);
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
        plugin.addCrystals(target, amount);
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
        plugin.removeCrystals(target, amount);
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
        int currentAmount = playerData.getData(CrescentNamespaceKeys.CRYSTALS_AMOUNT);
        int addedAmount = amount - currentAmount;
        if (addedAmount > 0) {
            GenerateCrystalsEvent event = new GenerateCrystalsEvent(addedAmount, CrystalGenerationSource.COMMAND, target);
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
        plugin.setCrystals(target, currentAmount + addedAmount);
        TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.SUCCESS, "You've set " + target.getName() + "'s Crystal(s) to " + amount + ".", String.valueOf(amount), target.getName());
        sender.sendMessage(textComponent);
    }
}
