package it.crescentsun.crystals;

import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.triumphcmd.bukkit.annotation.Permission;
import it.crescentsun.triumphcmd.core.annotations.Command;
import it.crescentsun.triumphcmd.core.annotations.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.DecoratedPot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
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

    @Permission("crescent.crystals.pot")
    @Command("pot")
    public void potCommand(CommandSender sender) {
        // Summon an invisible armor stand, being ridden by a decorated pot.
        if (!(sender instanceof Player player)) {
            TextComponent textComponent = MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "Only players can use this command.");
            sender.sendMessage(textComponent);
            return;
        }

    }
}
