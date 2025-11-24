package it.crescentsun.crescentcraft.artifact;

import it.crescentsun.api.artifacts.event.ArtifactInteractEvent;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.artifacts.item.tooltip.TooltipBuilder;
import it.crescentsun.api.artifacts.item.tooltip.TooltipStyle;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.sound.CompositeSoundEffect;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import org.bukkit.*;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static it.crescentsun.api.artifacts.item.tooltip.Tooltip.createHeader;

public class DetonationOrb extends Artifact {

    public static final int RADIUS = 2;
    public static final int DAMAGE = 20;
    public static final int FIRE_TICKS = 160;

    private final SoundEffect placeSound;
    private final CompositeSoundEffect readySound;
    private final SoundEffect ambientSound;
    private final SoundEffect explodeSound;

    public DetonationOrb(CrescentPlugin plugin, ArtifactFlag... defaultFlags) {
        super(
                plugin,
                ArtifactNamespacedKeys.DETONATION_ORB,
                new ItemStack(Material.FIRE_CHARGE, 1),
                "<#ff0000>Detonation Orb",
                TooltipStyle.DEFAULT,
                defaultFlags
        );
        placeSound = new SoundEffect(plugin, Sound.BLOCK_CORAL_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 0.5f);
        readySound = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_MAGMA_CUBE_DEATH, SoundCategory.PLAYERS, 0.75f, 0.5f),
                new SoundEffect(plugin, Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 0.75f, 1.2f));
        ambientSound = new SoundEffect(plugin, Sound.BLOCK_FIRE_AMBIENT, SoundCategory.AMBIENT, 0.6f, 1.45f);
        explodeSound = new SoundEffect(plugin, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 1.2f);

        defaultMeta.setMaxStackSize(16);
    }

    @Override
    protected void createTooltip() {
        tooltip = TooltipBuilder.builder()
                .page()
                    .section(createHeader("ATTRIBUTES", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getPrimaryHex1() + "Stacks up to 16")
                    .endSection()
                    .section(createHeader("DESCRIPTION", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getSecondaryHex1() + "May be placed on a surface to")
                        .addLine(tooltipStyle.getSecondaryHex1() + "detonate after a short delay,")
                        .addLine(tooltipStyle.getSecondaryHex1() + "dealing damage and setting")
                        .addLine(tooltipStyle.getSecondaryHex1() + "nearby entities on fire.")
                    .endSection()
                .endPage()
                .page()
                    .section(createHeader("STATS", tooltipStyle.getHeaderHex2()))
                        .addLine(tooltipStyle.getPrimaryHex2() + "Radius: {" + RADIUS + "} blocks")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Damage: {" + DAMAGE + "} HP")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Fire Duration: {" + (FIRE_TICKS / 20) + "} seconds")
                    .endSection()
                .endPage()
            .build();
    }

    @Override
    public boolean interactRight(ArtifactInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return false;
        }
        Location clickLoc = event.getClickedBlock().getLocation();
        World world = clickLoc.getWorld();
        placeSound.playAtLocation(clickLoc);
        ambientSound.playAtLocation(clickLoc);
        clickLoc.getBlock().setType(Material.DEAD_FIRE_CORAL_FAN);
        Waterlogged blockData = ((Waterlogged) clickLoc.getBlock().getBlockData());
        blockData.setWaterlogged(false);
        clickLoc.getBlock().setBlockData(blockData);
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        inventory.getItem(event.getHand()).setAmount(inventory.getItem(event.getHand()).getAmount() - 1);
        return true;
    }

    private class DetonationOrbRunnable extends BukkitRunnable {

        private final Player owner;
        private final Location location;
        private final World world;

        int clock = -1;

        public DetonationOrbRunnable(Player owner, Location location) {
            this.owner = owner;
            this.location = location;
            this.world = location.getWorld();
        }

        @Override
        public void run() {
            clock++;
            playLavaParticles();
            readyTrap();
            if (clock > 40) {

                playFlameParticles();
                playAmbientFire();
                List<LivingEntity> nearbyEntities = getEntitiesNearTrap();
                if (!nearbyEntities.isEmpty()) {
                    triggerTrap(nearbyEntities);
                    remove();
                }
            }
        }

        private void playLavaParticles() {            //Play lava particles every 0.25 seconds, before the trap is ready
            if (clock % 5 == 0 && clock < 40) {
                world.spawnParticle(Particle.LAVA, location, 1,
                        0, 0, 0, 0, null, true);
            }
        }

        private void readyTrap() {                                //Ready the trap. Make sounds effects and change block
            if (clock == 40) {
                location.getBlock().setType(Material.FIRE_CORAL_FAN);
                Waterlogged blockData = ((Waterlogged) location.getBlock().getBlockData());
                blockData.setWaterlogged(false);
                location.getBlock().setBlockData(blockData);
                readySound.playAtLocation(location);
            }
        }

        private void playFlameParticles() {                                  //Every 0.1 seconds, spawn a flame particle
            if (clock % 2 == 0) {
                world.spawnParticle(Particle.FLAME, location, 1,
                        0.25, 0.1, 0.25, 0, null, true);
            }
        }

        private void playAmbientFire() {                                      //Every 4 seconds, play ambient fire sound
            if (clock % 80 == 0) {
                ambientSound.playAtLocation(location);
            }
        }

        private List<LivingEntity> getEntitiesNearTrap() {
            List<LivingEntity> nearbyEntities = new ArrayList<>();
            for (Entity entity : location.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                if (!(entity instanceof LivingEntity victim) || entity.getUniqueId().equals(owner.getUniqueId())) continue;
                if (victim.hasLineOfSight(location)) {
                    nearbyEntities.add(victim);
                }
            }
            return nearbyEntities;
        }

        private void triggerTrap(List<LivingEntity> nearbyEntities) {          //If an entity is found, trigger the trap
            explodeSound.playAtLocation(location);
            world.spawnParticle(Particle.LAVA, location, 10,
                    1, 1, 1, 0, null, true);
            world.spawnParticle(Particle.EXPLOSION, location,
                    5, 1, 1, 1, 0, null, true);
            world.spawnParticle(Particle.FLAME, location, 25,
                    1, 1, 1, 0.25, null, true);
            //Damage and knock entities away.
            for (LivingEntity entity : nearbyEntities) {
                entity.damage(DAMAGE, owner);
                entity.setFireTicks(FIRE_TICKS);
                Vector unitVector = entity.getLocation().toVector().subtract(location.clone().add(0, -1, 0).toVector()).normalize();
                entity.setVelocity(unitVector.multiply(1.1 - (location.distance(location) / 4)));
            }
        }

        private void remove() {                                                //Remove the trap and cancel the runnable
            location.getBlock().setType(Material.AIR);
            cancel();
        }


    }
}
