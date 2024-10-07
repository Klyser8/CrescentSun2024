package it.crescentsun.crystals.artifact;

import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.artifacts.item.tooltip.*;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import it.crescentsun.crystals.Crystals;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static it.crescentsun.api.artifacts.item.tooltip.Tooltip.createHeader;

public class CrystalArtifact extends Artifact {
    private final Crystals crystalsPlugin;

    public CrystalArtifact(Crystals plugin, NamespacedKey key, ItemStack defaultStack, String displayNameText, TooltipStyle tooltipStyle, ArtifactFlag... flags) {
        super(plugin, key, defaultStack, displayNameText, tooltipStyle, flags);
        this.crystalsPlugin = plugin;

        defaultMeta.setMaxStackSize(99);
    }

    @Override
    protected void createTooltip() {
        tooltip = TooltipBuilder.builder()
                .page()
                    .section(createHeader("ATTRIBUTES", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getPrimaryHex1() + " Stacks up to 99")
                    .endSection()
                    .section(createHeader("DESCRIPTION", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getSecondaryHex1() + " Main form of currency")
                        .addLine(tooltipStyle.getSecondaryHex1() + " throughout the Crescent Sun")
                        .addLine(tooltipStyle.getSecondaryHex1() + " Network.")
                    .endSection()
                .endPage()
                .page()
                    .section(createHeader("ACTIONS", tooltipStyle.getHeaderHex2()))
                        .addLine(tooltipStyle.getPrimaryHex2() + " Insert into the Vault")
                        .addLine(tooltipStyle.getPrimaryHex2() + " for safekeeping.")
                    .endSection()
                .endPage()
            .build();
    }

    @Override
    public boolean onPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItem();
        if (!player.getUniqueId().equals(itemDrop.getThrower())) {
            if (itemDrop.getTicksLived() < crystalsPlugin.getSettings().getNonOwnedCrystalPickupDelay()) {
                event.setCancelled(true);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        // Set age
        new BukkitRunnable() {
            @Override
            public void run() {
                item.setTicksLived(Math.max(1, item.getTicksLived() - 3)); // Should make the item last 15000 ticks (12.5 minutes) instead of 6000 ticks (5 minutes)
                if (plugin.random().nextInt(4) == 0) {
                    item.getWorld().spawnParticle(Particle.WAX_OFF, item.getLocation().add(0.25, 0.25, 0.25), 1, 0.25, 0.25, 0.25, 1.0);
                }
                if (plugin.random().nextInt(8) == 0) {
                    crystalsPlugin.getCrystalsSFX().crystalHover.playAtLocation(item.getLocation());
                }
                if (item.isDead()) {
                    // Cancel task
                    cancel();
                }
            }
        }.runTaskTimer(crystalsPlugin, 5, 5);
        return true;
    }
}