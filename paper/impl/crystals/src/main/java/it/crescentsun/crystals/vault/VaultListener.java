package it.crescentsun.crystals.vault;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataIdentifier;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.event.AddCrystalsEvent;
import it.crescentsun.api.crystals.event.CreateVaultEvent;
import it.crescentsun.api.crystals.event.DestroyVaultEvent;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import it.crescentsun.crystals.artifact.CrystalArtifact;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultListener implements Listener {

    private final Crystals plugin;
    public VaultListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDropLand(PlayerDropItemEvent event) {
        Item itemDrop = event.getItemDrop();
        ItemStack itemStack = itemDrop.getItemStack();
        Artifact artifact = ArtifactUtil.identifyArtifact(itemStack);
        if (artifact == null) {
            return;
        }
        // Check if artifact is crystal
        if (!artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!itemDrop.isValid()) {
                    cancel();
                }
                boolean vaultStructureValid = VaultManager.isVaultStructureValid(itemDrop.getLocation().add(0, -0.5, 0));
                if (!vaultStructureValid) {
                    return;
                }
                Player owner = event.getPlayer();
                VaultData closestVault = plugin.getVaultManager().getClosestVault(itemDrop.getLocation());
                if (closestVault != null && (closestVault.getLocation().distanceSquared(itemDrop.getLocation()) <= 6)) { // Duplicate vault check
                    cancel();
                    owner.sendMessage(MiniMessage.miniMessage().deserialize(
                            CrescentHexCodes.RED + "A Crystal Vault is already present near this location!"
                    ));
                    return;
                }
                Location lodestoneLocation = itemDrop.getLocation().add(0, -1, 0);
                plugin.getVaultManager().createVault(owner, lodestoneLocation, false);
                MiniMessage miniMessage = MiniMessage.miniMessage();
                owner.sendMessage(miniMessage.deserialize(
                        CrescentHexCodes.FUCHSIA + " You've created a new Crystal Vault, at " +
                                CrescentHexCodes.YELLOW + "X: " + lodestoneLocation.getBlockX() +
                                ", Y: " + lodestoneLocation.getBlockY() +
                                ", Z: " + lodestoneLocation.getBlockZ() +
                                CrescentHexCodes.FUCHSIA + "! Right-click on it to deposit or withdraw your crystals."
                ));
                itemDrop.remove();
                cancel();
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (event.getBlock().getType() != Material.DIAMOND_BLOCK && event.getBlock().getType() != Material.LODESTONE) {
            return;
        }
        VaultData closestVault = plugin.getVaultManager().getClosestVault(loc);
        if (closestVault == null) {
            return;
        }
        double distanceFromClosestVault = closestVault.getLocation().distanceSquared(loc);

        Player player = event.getPlayer();
        if (distanceFromClosestVault <= 1) {
            if (closestVault.getOwnerUuid().equals(player.getUniqueId())) {
                DestroyVaultEvent vaultEvent = new DestroyVaultEvent(closestVault.getUuid(), player, closestVault.getLocation(), closestVault.isPublic(), true);
                vaultEvent.callEvent();
                if (vaultEvent.isCancelled()) {
                    return;
                }
                CompletableFuture<PluginDataIdentifier<PluginData>> completableFuture = closestVault.deleteAndSync();
                completableFuture.thenAccept(pluginDataIdentifier -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            CrescentHexCodes.YELLOW + "You have successfully destroyed your Crystal Vault at " +
                                    CrescentHexCodes.FUCHSIA + "X: " + closestVault.getLocation().getBlockX() +
                                    ", Y: " + closestVault.getLocation().getBlockY() +
                                    ", Z: " + closestVault.getLocation().getBlockZ() + "!"
                    ));
                    if (vaultEvent.shouldDropCrystal()) {
                        plugin.getCrystalsService().dropCrystals(player, closestVault.getLocation(), 1,
                                CrystalSource.BLOCK_DROP);
                    }
                });

            } else {
                event.setCancelled(true);
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        CrescentHexCodes.RED + "You cannot break this vault, as you are not its owner."
                ));
            }
        }
    }

    @EventHandler
    public void onVaultRightClick(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (!(rightClicked instanceof Interaction interactionEntity)) {
            return;
        }
        if (!interactionEntity.hasMetadata(VaultData.VAULT_KEY.getKey())) {
            return;
        }
        UUID vaultUuid = UUID.fromString(interactionEntity.getMetadata(VaultData.VAULT_KEY.getKey()).getFirst().asString());
        VaultData vaultData = plugin.getVaultManager().getDataInstance(vaultUuid);

        if (vaultData == null) {
            return;
        }

        // Now we're sure the player right-clicked a vault
        Player player = event.getPlayer();

        // Check that the player is the owner of the vault OR that the vault is public
        if (!vaultData.isPublic() && !vaultData.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    CrescentHexCodes.RED + "You cannot access this vault, as you are not its owner."
            ));
            return;
        } //TODO: 1. Fix inventory clicks 2. Avoid multiple vaults being placed in the same spot 3. Allow to destroy vaults

        ItemStack itemUsed = player.getInventory().getItemInMainHand();
        Artifact artifact = ArtifactUtil.identifyArtifact(itemUsed);
        if (artifact == null) {
            itemUsed = player.getInventory().getItemInOffHand();
            artifact = ArtifactUtil.identifyArtifact(itemUsed);
        }

        PlayerData playerData = plugin.getPlayerDataService().getData(player);
        int crystalsInVault = (int) playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT).orElse(0);
        if (artifact != null && artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
            int amountToDeposit;
            if (player.isSneaking()) {
                amountToDeposit = itemUsed.getAmount();
            } else {
                amountToDeposit = 1;
            }
            AddCrystalsEvent addCrystalsEvent = new AddCrystalsEvent(amountToDeposit, vaultUuid, player, vaultData.getLocation(), null, vaultData.isPublic());
            addCrystalsEvent.callEvent();
            if (addCrystalsEvent.isCancelled()) {
                return;
            }
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, crystalsInVault + addCrystalsEvent.getAddedAmount());
            itemUsed.setAmount(itemUsed.getAmount() - addCrystalsEvent.getAddedAmount());
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            CrescentHexCodes.FUCHSIA + "You have deposited " +
                                    CrescentHexCodes.YELLOW + addCrystalsEvent.getAddedAmount() +
                                    CrescentHexCodes.FUCHSIA + " crystals into your Crystal Vault! "
                    )
            );
            vaultData.refreshVaultNameTag();
        } else {
            // Open vault inventory
            VaultInventory vaultInventory = new VaultInventory(plugin, player, vaultData.getUuid());
            player.openInventory(vaultInventory.getInventory());
        }
    }

    @EventHandler
    public void onVaultInventoryClick(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof VaultInventory vaultInventory)) {
            return;
        }
        // Count all crystal artifacts in the inventory

        Player owner = vaultInventory.getOwner();

        int crystalCount = 0;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                Artifact artifact = ArtifactUtil.identifyArtifact(item);
                if (artifact != null && artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
                    crystalCount += item.getAmount();
                }
            }
        }
        plugin.getPlayerDataService().getData(owner).updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, crystalCount);
        VaultData vaultData = plugin.getVaultManager().getDataInstance(vaultInventory.getVaultUUID());
        vaultData.refreshVaultNameTag();
    }
}
