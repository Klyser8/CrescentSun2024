package it.klynet.crystals;

import it.klynet.adventurousklynetmsg.MessageFormatter;
import it.klynet.adventurousklynetmsg.MessageType;
import it.klynet.klynetcore.api.PlayerUtils;
import it.klynet.klynetcore.api.event.crystals.CrystalGenerationSource;
import it.klynet.klynetcore.api.event.crystals.GenerateCrystalsEvent;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import it.klynet.klynetcore.cmd.bukkit.annotation.Permission;
import it.klynet.klynetcore.cmd.core.BaseCommand;
import it.klynet.klynetcore.cmd.core.annotation.Command;
import it.klynet.klynetcore.cmd.core.annotation.Default;
import it.klynet.klynetcore.cmd.core.annotation.Optional;
import it.klynet.klynetcore.cmd.core.annotation.SubCommand;
import it.klynet.klynetcore.core.data.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys.SETTINGS_SHOW_CRYSTALIX;

@Command(value = "crystals", alias = "cr")
public class CrystalsCommands extends BaseCommand {

    private final Crystals plugin;
    public CrystalsCommands(Crystals plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission("klynet.crystals")
    public void defaultCommand(CommandSender sender) {
        TextComponent text = MessageFormatter.formatCommandMessage(
                MessageType.INFO, Component.text("Type \"/crystals help\" for help."),
                "/crystals help");
        sender.sendMessage(text);
    }

    @SubCommand("toggle")
    @Permission("klynet.crystals.toggle")
    public void toggleCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(textComponent);
            return;
        }
        PlayerData playerData = plugin.getKlyNetCore().getPlayerManager().getData(player.getUniqueId());
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
    @Permission("klynet.crystals.spawn")
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
    @Permission("klynet.crystals.add")
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
    @Permission("klynet.crystals.remove")
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
    @Permission("klynet.crystals.set")
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
        int currentAmount = playerData.getData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT);
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
