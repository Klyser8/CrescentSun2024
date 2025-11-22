package it.crescentsun.crystals.vault;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.AdvancementUtil;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.event.AddCrystalsEvent;
import it.crescentsun.api.crystals.event.CreateVaultEvent;
import it.crescentsun.api.crystals.event.DestroyVaultEvent;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import it.crescentsun.api.crescentcore.event.player.PlayerDataSavedPostQuitEvent;
import it.crescentsun.api.crescentcore.event.player.PlayerJoinEventPostDBLoad;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
            private int tickCount = 0;
            private boolean vaultStructureValid;
            private int extraCrystalsToAdd;

            @Override
            public void run() {
                if (!itemDrop.isValid()) {
                    cancel();
                    return;
                }
                if (!vaultStructureValid) {
                    vaultStructureValid = VaultManager.isVaultStructureValid(itemDrop.getLocation().add(0, -0.3 - tickCount / 4.0, 0));
                    return;
                }
                Player owner = event.getPlayer();
                VaultData closestVault = plugin.getVaultManager().getClosestVault(itemDrop.getLocation());
                if (closestVault != null && (closestVault.getLocation().distanceSquared(itemDrop.getLocation()) <= 6)) {
                    cancel();
                    owner.sendMessage(MiniMessage.miniMessage().deserialize(CrescentHexCodes.RED + "A Crystal Vault is already present near this location!"));
                    return;
                }

                // Collect & immediately deposit any additional nearby crystal items (same owner) before finalization
                if (tickCount < 4) {
                    for (Entity entity : itemDrop.getNearbyEntities(1, 1, 1)) {
                        if (entity instanceof Item nearbyItem && nearbyItem != itemDrop) {
                            Artifact nearbyArtifact = ArtifactUtil.identifyArtifact(nearbyItem.getItemStack());
                            if (nearbyArtifact != null && nearbyArtifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
                                if (nearbyItem.getOwner() != null && !nearbyItem.getOwner().equals(owner.getUniqueId())) {
                                    continue;
                                }
                                extraCrystalsToAdd += nearbyItem.getItemStack().getAmount();
                                nearbyItem.remove();
                            }
                        }
                    }
                }

                World world = itemDrop.getWorld();
                Location centre = itemDrop.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
                if (tickCount == 0) {
                    itemDrop.teleport(centre);
                    Location above = centre.clone().add(0, 1, 0);
                    itemDrop.setGravity(false);
                    itemDrop.setInvulnerable(true);
                    world.spawnParticle(Particle.OMINOUS_SPAWNING, above, 250, 0, 0, 0, 10);
                    world.spawnParticle(Particle.END_ROD, itemDrop.getLocation().add(0, 0.3, 0), 15, 0.05, 0.05, 0.05, 0.01);
                    itemDrop.setVelocity(new Vector(0, 0.033f, 0));
                }
                if (tickCount == 2) {
                    plugin.getCrystalsSFX().vaultCreate.playAtLocation(centre.clone().add(0, 1, 0));
                } else if (tickCount == 4) {
                    world.spawnParticle(Particle.FISHING, centre, 100, 0, 0, 0, 0.05);
                    world.spawnParticle(Particle.FIREWORK, centre, 25, 0.25, 0.25, 0.25, 0.2);
                    world.spawnParticle(Particle.FLASH, centre, 1, Color.BLUE);
                    Location lodestoneLocation = itemDrop.getLocation().add(0, -1, 0);
                    VaultData playerVault = plugin.getVaultManager().createVault(owner, lodestoneLocation, false);

                    // Consume 1 crystal for vault creation then deposit any remaining from original stack
                    int originalAmount = itemDrop.getItemStack().getAmount();
                    if (originalAmount > 0) {
                        itemDrop.getItemStack().setAmount(originalAmount - 1); // consume one
                    }
                    PlayerData playerData = plugin.getPlayerDataService().getData(owner);
                    int remaining = itemDrop.getItemStack().getAmount();
                    if (remaining > 0) {
                        int crystalsInVault = (int) playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT).orElse(0);
                        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, crystalsInVault + remaining);
                    }
                    itemDrop.remove();
                    playerVault.refreshVaultNameTag();
                    if (extraCrystalsToAdd > 0) {
                        int currentVaultCrystals = (int) playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT).orElse(0);
                        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT,
                                currentVaultCrystals + extraCrystalsToAdd);
                    }
                    String extraCrystalsMsg = extraCrystalsToAdd > 0 ?
                            "\n<#52d1ff>Additionally, </#52d1ff><white>"  + extraCrystalsToAdd +
                                    "</white><#52d1ff> extra crystals were deposited into the vault from nearby dropped artifacts.\n" +
                                    "<#15ffcc>-----------------------------------------------------</#15ffcc>" : "";
                    String raw = "\n" +
                            "<#15ffcc>----------------------</#15ffcc>" +
                            "<#52d1ff> CRYSTALS </#52d1ff>" +
                            "<#15ffcc>----------------------</#15ffcc>\n" +
                            "<#88f5ff>You made a new <b>Crystal Vault</b> at <white>%d</white>, <white>%d</white>, <white>%d</white>.</#88f5ff>\n" +
                            "<#52d1ff>- Right-click to open, and deposit/withdraw Crystals.</#52d1ff>\n" +
                            "<#88f5ff>- The Crystal Vault is yours only, with its contents being accessible throughout the whole network.</#88f5ff>\n" +
                            "<#52d1ff>- Right-click while holding a Crystal to deposit it in the vault.</#52d1ff>\n" +
                            "<#15ffcc>-----------------------------------------------------</#15ffcc>" +
                            extraCrystalsMsg;
                    owner.sendMessage(MiniMessage.miniMessage().deserialize(String.format(raw, playerVault.getX(), playerVault.getY(), playerVault.getZ())));
                    cancel();
                }
                tickCount++;

            }
        }.runTaskTimer(plugin, 0, 6);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location breakLoc = event.getBlock().getLocation();
        if (!isLocationPartOfVault(breakLoc)) {
            return;
        }
        VaultData closestVault = plugin.getVaultManager().getClosestVault(breakLoc);
        if (closestVault == null) {
            return;
        }
        Player player = event.getPlayer();
        if (closestVault.getOwnerUuid().equals(player.getUniqueId())) {
            DestroyVaultEvent vaultEvent = new DestroyVaultEvent(closestVault.getUuid(), player, closestVault.getLocation(), closestVault.isPublic(), true);
            vaultEvent.callEvent();
            if (vaultEvent.isCancelled()) {
                return;
            }
            VaultData vault = plugin.getVaultManager().deleteVault(closestVault.getUuid());
            plugin.getCrystalsSFX().vaultBreak.playAtLocation(breakLoc);
            String raw = CrescentHexCodes.RED + "You have destroyed the Crystal Vault found at <yellow>%d<white>, <yellow>%d<white>, <yellow>%d<white>.";

            String formatted = String.format(raw, vault.getX(), vault.getY(), vault.getZ());
            player.sendMessage(MiniMessage.miniMessage().deserialize(formatted));
            if (vaultEvent.shouldDropCrystal()) {
                plugin.getCrystalsService().dropCrystals(player, vault.getLocation().add(0, 2, 0), 1, CrystalSource.BLOCK_DROP);
            }
        } else {
            event.setCancelled(true);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    CrescentHexCodes.RED + "You cannot break this vault, as you are not its owner."
            ));
        }
    }

    @EventHandler
    public void onVaultCreate(CreateVaultEvent event) {
        Player owner = event.getOwner();
        // Award advancement, if in crescentcraft and if not already awarded
        if (!plugin.getCrescentCoreAPI().getServerName().equalsIgnoreCase("crescentcraft")) {
            return;
        }

        AdvancementUtil.awardAdvancementCriteria(owner, "crescentsun:crescentcraft/build_vault", "build_vault");
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (isLocationPartOfVault(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // only sticky pistons can pull blocks
        if (!event.isSticky()) return;
        for (Block b : event.getBlocks()) { //
            if (isLocationPartOfVault(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // prevent vault blocks from being destroyed
        event.blockList().removeIf(b -> isLocationPartOfVault(b.getLocation()));
    }

    @EventHandler
    public void onVaultRightClick(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (!(rightClicked instanceof Interaction interactionEntity)) {
            return;
        }
        PersistentDataContainer pdc = interactionEntity.getPersistentDataContainer();
        String vaultStringUuid = pdc.get(VaultData.VAULT_KEY, PersistentDataType.STRING);
        if (vaultStringUuid == null) {
            return;
        }
        UUID vaultUuid = UUID.fromString(vaultStringUuid);
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
        }

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
            plugin.getCrystalsSFX().vaultOpen.playForPlayerAtLocation(player);
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
        plugin.getCrystalsSFX().vaultClose.playForPlayerAtLocation(owner);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEventPostDBLoad event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (UUID vaultId : plugin.getVaultManager().getVaultsByOwner(uuid)) {
            VaultData data = plugin.getVaultManager().getDataInstance(vaultId);
            if (data != null && !data.isPublic()) {
                data.startTask();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerDataSavedPostQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (UUID vaultId : plugin.getVaultManager().getVaultsByOwner(uuid)) {
            VaultData data = plugin.getVaultManager().getDataInstance(vaultId);
            if (data != null && !data.isPublic()) {
                data.stopTask();
            }
        }
    }

    /**
     * Returns true if the block at this location is either
     *  • a diamond block
     *  • a lodestone
     *  • and is within distanceSquared ≤ 1 of the closest vault.
     */
    private boolean isLocationPartOfVault(Location loc) {
        if (loc.getBlock().getType() != Material.DIAMOND_BLOCK && loc.getBlock().getType() != Material.LODESTONE) {
            return false;
        }
        VaultData closestVault = plugin.getVaultManager().getClosestVault(loc);
        if (closestVault == null) {
            return false;
        }
        if (closestVault.getLocation().distanceSquared(loc) > 2.0) {
            return false;
        }

        for (Map.Entry<Vector, Material> entry : VaultManager.vaultBlockOffsets.entrySet()) {
            Vector offset = entry.getKey().clone();
            offset.add(new Vector(0, -1, 0));
            Location checkLoc = closestVault.getLocation().clone()
                    .add(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            if (loc.equals(checkLoc)) {
                return true;
            }
        }
        return false;
    }
}
