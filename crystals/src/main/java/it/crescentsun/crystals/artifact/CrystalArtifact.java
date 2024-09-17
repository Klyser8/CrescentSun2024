package it.crescentsun.crystals.artifact;

import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.ArtifactFlag;
import it.crescentsun.crystals.Crystals;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class CrystalArtifact extends Artifact {
    private final Crystals plugin;

    public CrystalArtifact(Crystals plugin, NamespacedKey key, ItemStack defaultStack, String displayName, ArtifactFlag... flags) {
        super(key, defaultStack, displayName, flags);
        this.plugin = plugin;
    }

    @Override
    public boolean onPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItem();
        if (!player.getUniqueId().equals(itemDrop.getThrower())) {
            if (itemDrop.getTicksLived() < plugin.getSettings().getNonOwnedCrystalPickupDelay()) {
                event.setCancelled(true);
                return false;
            }
        }
        return true;
    }

}