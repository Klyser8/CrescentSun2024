package it.crescentsun.crystals.crystalix;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.VectorUtils;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.crystals.Crystals;
import it.crescentsun.crystals.artifact.CrystalArtifact;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Represents a task to handle Crystalix behavior, including movement, visual effects, and interaction with Crystals.
 * IMPORTANT NOTE: A crystalix will only pick up crystals that have the thrower set to the owner's UUID.
 */
@Deprecated
public class CrystalixTask implements Consumer<BukkitTask> {

    public static final int INTERVAL = 1;
    public static final int START_TICKS = 20; // Duration of the start-off transition. Higher = slower start-off.
    public static final double SPEED_MULTIPLIER = 0.035; // Higher = faster
    private static final double Y_OFFSET = 1.25;

    private final Crystals plugin;
    private final Random random;
    private final CrystalixEntity crystalix;
    private final Player owner;
    private final PlayerData ownerData;
    private final World world;
    private int ticks;
    private int movementTicks;
    private final Item crystalixItem;
    private Item targetCrystal = null;
    private Location prevTargetPos = null;

    /**
     * Initializes a new instance of the CrystalixTask class.
     *
     * @param plugin    The Crystals plugin instance.
     * @param crystalix The Crystalix entity instance.
     */
    public CrystalixTask(Crystals plugin, CrystalixEntity crystalix) {
        this.plugin = plugin;
        this.random = plugin.random();
        this.crystalix = crystalix;
        this.owner = crystalix.getOwner();
        this.ownerData = crystalix.getOwnerData();
        this.world = owner.getWorld();
        crystalixItem = crystalix.item;
    }

    /**
     * Handles the Crystalix behavior during each tick.
     *
     * @param bukkitTask The Bukkit task instance.
     */
    @Override
    public void accept(BukkitTask bukkitTask) {
        //Remove crystalix if the owner disabled it
//        boolean isCrystalixShowing = ownerData.getData(CrescentNamespaceKeys.SETTINGS_SHOW_CRYSTALIX);
//        if (!isCrystalixShowing) {
//            crystalix.delete();
//        }
        // Remove crystalix if the owner is offline
        /*if (!owner.isOnline()) {
            crystalix.delete();
        }*/
        // Cancel task if item is dead
        if (crystalixItem.isDead()) {
            bukkitTask.cancel();
        }
//        int crystals = ownerData.getDataValue(PLAYERS_CRYSTAL_AMOUNT);
//        if (crystalix.getWrittenCrystals() != crystals) {
//            crystalix.updateName();
//        }

        // Set initial velocity
        if (ticks == 0) {
            crystalixItem.setVelocity(new Vector(0, 0, 0));
        }

        // Set custom name visibility based on the owner's sneaking status
        if (owner.isSneaking() && crystalixItem.isCustomNameVisible()) {
            crystalixItem.setCustomNameVisible(false);
        } else if (!owner.isSneaking() && !crystalixItem.isCustomNameVisible()) {
            crystalixItem.setCustomNameVisible(true);
        }

        playVFX();
        handlePosition(crystalixItem);
        if (targetCrystal != null) {
            if (targetCrystal.getLocation().distance(crystalixItem.getLocation()) < 1) {
//                plugin.addCrystals(owner, targetCrystal.getItemStack().getAmount());
//                ownerData.updateDataValue(PLAYERS_CRYSTAL_AMOUNT, crystals + targetCrystal.getItemStack().getAmount());
                targetCrystal.remove();
                world.playSound(crystalixItem.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
                world.playSound(crystalixItem.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_FALL, 1f, 1.5f);
                targetCrystal = null;
            }
        }

        //If the targetKlyne is dead, set it to null.
        if (targetCrystal != null && targetCrystal.isDead()) {
            targetCrystal = null;
        }

        ticks++;
    }

    private void playVFX() {
        double rnd = random.nextDouble();
        World world = owner.getWorld();
        Location vfxLoc = crystalix.item.getLocation().add(0, 0.2, 0);
        if (rnd < 0.25) {
            world.spawnParticle(Particle.WITCH, vfxLoc,
                    1, 0.1, 0.1, 0.1, 0);
        } else {
            if (rnd < 0.30) {
                world.spawnParticle(Particle.CHERRY_LEAVES, vfxLoc,
                        1, 0.1, 0.1, 0.1, 0);
            } else if (rnd < 0.35) {
                world.spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, vfxLoc,
                        1, 0.1, 0.1, 0.1, 0);
            }
        }
    }

    private void handlePosition(Item item) {
        Location prevTargetPos = targetCrystal == null ? owner.getLocation().add(0, Y_OFFSET, 0) : targetCrystal.getLocation();
        //If Crystalix has a target klyne, set the target position to that klyne's location. Otherwise, set it to null.
        Location targetPos = targetCrystal == null ? null : targetCrystal.getLocation();
        //If this statement is true, Crystalix has no target Klyne.
        if (targetPos == null && random.nextDouble() < 0.025) {
            targetPos = findNearestKlyne(item.getLocation());
        }
        //If after all that, Crystalix still doesn't have a target klyne, set the target position to the owner's location.
        if (targetPos == null) {
            targetPos = owner.getLocation().add(0, Y_OFFSET, 0);
        }

        if (!targetPos.toVector().equals(prevTargetPos.toVector())) {
            movementTicks = 0; // Reset the movementTicks counter if the target position changes
        }
        prevTargetPos = targetPos.clone(); // Store the current target position as the previous one for the next iteration


        Vector crystalixPos = item.getLocation().toVector();
        double distanceFromOwner = crystalixPos.distance(owner.getLocation().toVector());
        double distanceFromTarget = crystalixPos.distance(targetPos.toVector());
        //Minimum distance the Crystalix keeps from the target position.
        double minDistanceFromTarget = targetCrystal == null ? 2.5 : 0.5;
        // Teleport Crystalix if it's too far away from the owner.
        if ((distanceFromOwner > 20 && targetCrystal == null) || (distanceFromOwner > 40 && targetCrystal != null)) {
            item.teleport(targetPos);
        } else if (distanceFromTarget > minDistanceFromTarget) {
            // Calculate and set velocity for the item to move towards the target position
            Vector direction = targetPos.toVector().subtract(crystalixPos).normalize();
            double speed = Math.min(0.5, distanceFromTarget * SPEED_MULTIPLIER); // Limit the maximum speed

            // Use an easing function to approach the target smoothly
            double t = Math.min(1, (double) movementTicks / START_TICKS);
            double easingFactor = t * t * (3 - 2 * t);
            speed = speed * easingFactor;

            Vector desiredVelocity = direction.multiply(speed);
            // LERP between the current velocity and desired velocity to achieve a smooth transition
            double lerpFactor = 0.1; // Adjust this value to control the smoothness of the transition
            Vector newVelocity = item.getVelocity().multiply(1 - lerpFactor).add(desiredVelocity.multiply(lerpFactor));
            item.setVelocity(newVelocity);
        } else {
            // Gradually slow down the item to a halt
            Vector currentVelocity = item.getVelocity();
            Vector newVelocity = currentVelocity.multiply(0.9); // Higher value = slower deceleration
            item.setVelocity(newVelocity);
        }
        movementTicks++; // Increment the movementTicks counter
    }

    private Location findNearestKlyne(Location location) {
        double searchRadius = 8; // Adjust the search radius as needed
        Item nearestKlyne = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : location.getWorld().getNearbyEntities(location, searchRadius, searchRadius, searchRadius)) {
            if (entity instanceof Item itemDrop) {
                ItemStack itemStack = itemDrop.getItemStack();
                boolean isThrowerCrystalixOwner = owner.getUniqueId().equals(itemDrop.getThrower()) || itemDrop.getThrower() == null;
                boolean canPickup = itemDrop.getPickupDelay() == 0 && itemDrop.canPlayerPickup();
                if (isStackCrystal(itemStack) && isThrowerCrystalixOwner && canPickup) {
                    double distance = location.distance(entity.getLocation());
                    if (distance < minDistance && VectorUtils.hasLineOfSight(location, entity.getLocation())) {
                        nearestKlyne = itemDrop;
                        minDistance = distance;
                    }
                }
            }
        }
        if (nearestKlyne != null) {
            targetCrystal = nearestKlyne;
            crystalixItem.getWorld().playSound(
                    crystalixItem.getLocation(), "block.amethyst_block.resonate", 0.75f, 1.5f);
        }
        return nearestKlyne != null ? nearestKlyne.getLocation() : null;
    }

    private boolean isStackCrystal(ItemStack itemStack) {
        Artifact artifactItem = ArtifactUtil.identifyArtifact(itemStack);
        return artifactItem instanceof CrystalArtifact;
    }
}

