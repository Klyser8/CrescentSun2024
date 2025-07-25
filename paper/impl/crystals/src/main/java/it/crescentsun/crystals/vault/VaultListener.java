package it.crescentsun.crystals.vault;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
                Location lodestoneLocation = itemDrop.getLocation().add(0, -1, 0);
                Player owner = event.getPlayer();
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

}
