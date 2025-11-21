package it.crescentsun.crystals.vault;

import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.crystals.Crystals;
import it.crescentsun.triumphcmd.bukkit.annotation.Permission;
import it.crescentsun.triumphcmd.core.annotations.Command;
import it.crescentsun.triumphcmd.core.annotations.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.joml.Vector3i;

import java.util.UUID;

@Command(value = "vault", alias = "v")
public class VaultCommands {

    private final Crystals plugin;
    private final VaultManager vaultManager;

    public VaultCommands(Crystals plugin) {
        this.plugin = plugin;
        this.vaultManager = plugin.getVaultManager();
    }

    @Permission("crescent.vault")
    @Command
    public void defaultCommand(CommandSender sender) {
        String raw = "<#FFDE59>Type '/vault help' for help.";
        TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
        sender.sendMessage(textComponent);
    }

    @Permission("crescent.vault.help")
    @Command("help")
    public void helpCommand(CommandSender sender) {
        // Show the player the commands for which they have permission
        StringBuilder helpMessage = new StringBuilder("\n<#86CDDC>Vault Commands:\n");
        if (sender.hasPermission("crescent.vault.create")) {
            helpMessage.append("<#FFFFFF>/vault create<#86CDDC> - Create a new vault at your current location.\n");
        }
        if (sender.hasPermission("crescent.vault.list")) {
            helpMessage.append("<#FFFFFF>/vault list<#86CDDC> - List the vaults you own on this server.\n");
        }
        if (sender.hasPermission("crescent.vault.list.other")) {
            helpMessage.append("<#FFFFFF>/vault list <player><#86CDDC> - List the vaults of another player on this server.\n");
        }
        if (sender.hasPermission("crescent.vault.delete")) {
            helpMessage.append("<#FFFFFF>/vault delete <x,y,z><#86CDDC> - Delete a vault at the specified coordinates, or where you're looking if none are provided.\n");
        }
        helpMessage.append("<#FFFFFF>/vault help<#86CDDC> - Show this help message.");
        TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(helpMessage.toString());
        sender.sendMessage(textComponent);
    }

    @Permission("crescent.vault.create")
    @Command("create")
    public void vaultCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            String raw = "<#DE0A0A>Only players can use this command.";
            TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
            sender.sendMessage(textComponent);
            return;
        }
        Location vaultOrigin = player.getLocation().add(0, 0, 0);
        for (Vector blockLoc : VaultManager.vaultBlockOffsets.keySet()) {
            Material blockType = VaultManager.vaultBlockOffsets.get(blockLoc);
            vaultOrigin.getWorld().setBlockData(
                    vaultOrigin.clone().add(blockLoc.getBlockX(), blockLoc.getBlockY() - 1, blockLoc.getBlockZ()),
                    blockType.createBlockData()
            );
        }
        plugin.getVaultManager().createVault(player, vaultOrigin, true);
        // Announce creation of vault to all players on the server
        String raw = "<#86CDDC>" + player.getName() + " has created a new <u>public</u> vault at " +
                "<#ffffff>" + player.getLocation().getWorld().getName() + " (" +
                player.getLocation().getBlockX() + ", " +
                player.getLocation().getBlockY() + ", " +
                player.getLocation().getBlockZ() + ")";
        TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(textComponent);
        }
    }

    @Permission("crescent.vault.delete")
    @Command("delete")
    public void deleteCommand(CommandSender sender, @Optional String coordsSplitByComma) {
        if (!(sender instanceof Player player)) {
            String raw = "<#DE0A0A>Only players can use this command.";
            TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
            sender.sendMessage(textComponent);
            return;
        }

        if (coordsSplitByComma == null) {
            // Get the entity the player is looking at within 5 blocks. If it's a vault, delete it.
            Entity targetEntity = player.getTargetEntity(5);
            if (targetEntity == null) {
                String raw = "<#DE0A0A>No Vault found in sight within 5 blocks. Please specify coordinates to delete a vault.";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
                return;
            }
            PersistentDataContainer pdc = targetEntity.getPersistentDataContainer();
            String vaultStringUuid = pdc.get(VaultData.VAULT_KEY, PersistentDataType.STRING);
            if (vaultStringUuid == null) {
                String raw = "<#DE0A0A>The entity you are looking at is not a Vault. Please specify coordinates to delete one.";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
                return;
            }
            UUID vaultUuid = UUID.fromString(vaultStringUuid);
            VaultData vault = vaultManager.deleteVault(vaultUuid);
            if (vault == null){
                String raw = "<#DE0A0A>Something went wrong while trying to delete the vault.";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
            } else {
                String raw = "<#FFDE59>Deleted vault at " +
                        "<#ffffff>" + vault.getLocation().getWorld().getName() + " (" +
                        vault.getLocation().getBlockX() + ", " +
                        vault.getLocation().getBlockY() + ", " +
                        vault.getLocation().getBlockZ() + ")";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
            }
        }
        else {
            // Delete the vault at the specified coordinates
            String[] coords = coordsSplitByComma.split(",");
            int vaultX, vaultY, vaultZ;
            try {
                vaultX = Integer.parseInt(coords[0].trim());
                vaultY = Integer.parseInt(coords[1].trim());
                vaultZ = Integer.parseInt(coords[2].trim());
            } catch (NumberFormatException e) {
                String raw = "<#DE0A0A>Invalid coordinates provided. Please provide valid X, Y, and Z coordinates, split with commas";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
                return;
            }
            VaultData vault = vaultManager.deleteVault(new Location(player.getWorld(), vaultX, vaultY, vaultZ));
            if (vault == null) {
                String raw = "<#DE0A0A>Could not find a vault at the specified coordinates. Are you in the right world / server?";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
            } else {
                String raw = "<#FFDE59>Deleted vault at " +
                        "<#ffffff>" + vault.getLocation().getWorld().getName() + " (" +
                        vault.getLocation().getBlockX() + ", " +
                        vault.getLocation().getBlockY() + ", " +
                        vault.getLocation().getBlockZ() + ")";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
            }
        }
    }

    @Permission("crescent.vault.list")
    @Command("list")
    public void listCommand(CommandSender sender, @Optional Player player) {
        boolean canPlayerSeeOtherPlayersVaults = sender.hasPermission("crescent.vault.list.other");
        if (player != null && !canPlayerSeeOtherPlayersVaults && !player.getName().equalsIgnoreCase(sender.getName())) {
            String raw = "<#DE0A0A>You do not have permission to list other players' vaults.";
            TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
            sender.sendMessage(textComponent);
        }
        if (player == null) {
            if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                String raw = "<#DE0A0A>Specify a player to list their vaults.";
                TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
                sender.sendMessage(textComponent);
                return;
            }
        }
        UUID[] vaultUUIDs = vaultManager.getVaultsByOwner(player.getUniqueId());
        if (vaultUUIDs.length == 0) {
            String raw = "<#FFDE59>" + player.getName() + " has no vaults.";
            TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(raw);
            sender.sendMessage(textComponent);
            return;
        }
        StringBuilder sb = new StringBuilder("<#86CDDC>Vaults for<#FFFFFF> " + player.getName() + " <#86CDDC>in<#FFFFFF> " + plugin.getCrescentCoreAPI().getServerName() + "<#86CDDC>: \n");
        for (UUID vaultUUID : vaultUUIDs) {
            VaultData vaultData = vaultManager.getDataInstance(vaultUUID);
            if (vaultData == null) {
                plugin.getLogger().warning("Could not get vault data for " + vaultUUID);
            } else {
                sb.append("<#86CDDC> - Location: ")
                        .append("<#ffffff>").append(vaultData.getLocation().getWorld().getName())
                        .append(" (").append(vaultData.getLocation().getBlockX()).append(", ")
                        .append(vaultData.getLocation().getBlockY()).append(", ")
                        .append(vaultData.getLocation().getBlockZ()).append(")");
                // If it is public, append "[public]" in yellow
                if (vaultData.isPublic()) {
                    sb.append(" <#FFDE59>[Public Vault]");
                }
                // Add new line ONLY if it's not the last vault
                if (vaultUUID != vaultUUIDs[vaultUUIDs.length - 1]) {
                    sb.append("\n");
                }
            }

        }
        TextComponent textComponent = (TextComponent) MiniMessage.miniMessage().deserialize(sb.toString());
        sender.sendMessage(textComponent);
    }

}
