package it.crescentsun.crystals;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.VectorUtils;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.api.crystals.CrystalsService;
import it.crescentsun.api.crystals.event.DecrementCrystalsEvent;
import it.crescentsun.api.crystals.event.GenerateCrystalsEvent;
import it.crescentsun.api.crystals.event.IncrementCrystalsEvent;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CrystalManager implements CrystalsService {

    private final Crystals plugin;
    public CrystalManager(Crystals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnCrystals(Player player, int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation) {
        if (amount > spawnAnimation.getMax() || amount < spawnAnimation.getMin()) {
            plugin.getLogger().warning("Amount of crystals to spawn is out of bounds for the animation. Defaulting to the minimum");
            amount = spawnAnimation.getMin();
        }
        GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, source, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        int finalAmount = Math.min(event.getAmount(), 99);
        ItemStack crystalStack = plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL).createStack(finalAmount);
        ArtifactUtil.addFlagsToStack(crystalStack, ArtifactFlag.HIDE_DROP_NAME);
        if (spawnAnimation == CrystalSpawnAnimation.CIRCLING_EXPLOSION) {
            circlingExplosion(player, amount, crystalStack, source, player.getLocation().clone().add(0, 3, 0));
        } else if (spawnAnimation == CrystalSpawnAnimation.HOVER) {
            hover(player, crystalStack, source, spawnLocation);
        }
    }

    private void hover(Player player, ItemStack crystalStack, CrystalSource source, Location spawnLocation) {
        AtomicInteger movementTicks = new AtomicInteger();
        AtomicReference<Location> previousTargetLoc = new AtomicReference<>(player.getLocation());
        // item entity should stay in place for about two seconds, then move 50% of the way between it and the player who won it.
        Item crystal = player.getWorld().dropItem(spawnLocation, crystalStack);
        crystal.setGravity(false);
        crystal.setVelocity(new Vector(0, 0, 0));
        crystal.setCanPlayerPickup(false);
        World world = crystal.getWorld();
        plugin.getCrystalsSFX().crystalAppear.playForPlayerAtLocation(player);
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
            int ticksLived = crystal.getTicksLived();
            if (ticksLived < 40) {
                // Reset velocity and position to keep the crystal stationary
                crystal.setVelocity(new Vector(0, 0, 0));
                //TODO: refactor this mess
                world.spawnParticle(Particle.DOLPHIN, crystal.getLocation().clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                if (crystal.getTicksLived() % 4 == 0) {
                    world.spawnParticle(Particle.FIREWORK, crystal.getLocation().clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                }
            }
            if (ticksLived >= 40) {
                movementTicks.incrementAndGet();
                // Apply velocity so that it slowly goes towards the player, for the next 20 seconds - slowing down as it gets closer.
                Location targetLoc = player.getLocation().clone().add(0, 1, 0);
                if (!targetLoc.toVector().equals(previousTargetLoc.get().toVector())) {
                    movementTicks.set(0);
                }
                previousTargetLoc.set(targetLoc);
                Location crystalLoc = crystal.getLocation().clone();
                double distanceFromPlayer = crystalLoc.distance(targetLoc);
                // Max distance should be 50% of the way between the player and the crystal.
                double maxDistanceFromTarget = distanceFromPlayer / 2;

                //TODO: refactor this mess
                world.spawnParticle(Particle.DOLPHIN, crystal.getLocation().clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                if (crystal.getTicksLived() % 4 == 0) {
                    world.spawnParticle(Particle.FIREWORK, crystal.getLocation().clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                }

                if (distanceFromPlayer > maxDistanceFromTarget) {
                    Vector direction = VectorUtils.getDirection(crystalLoc.toVector(), targetLoc.toVector());
                    double speed = Math.min(0.5, distanceFromPlayer * 0.035);
                    double t = Math.min(1, (double) movementTicks.get() / 10);
                    double easingFactor = t * t * (3 - 2 * t);
                    speed = speed * easingFactor;

                    Vector desiredVelocity = direction.multiply(speed);
                    // LERP between the current velocity and desired velocity to achieve a smooth transition
                    double lerpFactor = 0.1; // Adjust this value to control the smoothness of the transition
                    Vector newVelocity = crystal.getVelocity().multiply(1 - lerpFactor).add(desiredVelocity.multiply(lerpFactor));
                    crystal.setVelocity(newVelocity);
                } else {
                    // Gradually slow down the item to a halt
                    Vector currentVelocity = crystal.getVelocity();
                    Vector newVelocity = currentVelocity.multiply(0.9); // Higher value = slower deceleration
                    crystal.setVelocity(newVelocity);
                }
            }
            if (ticksLived == 60) {
                IncrementCrystalsEvent incrementEvent = new IncrementCrystalsEvent(1, source);
                incrementEvent.callEvent();
                if (incrementEvent.isCancelled()) {
                    crystal.remove();
                    bukkitTask.cancel();
                    return;
                }

                crystal.setGravity(true);
                crystal.setPickupDelay(20);
                crystal.setCanPlayerPickup(true);
                crystal.setThrower(player.getUniqueId());
                ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.HIDE_DROP_NAME);

                bukkitTask.cancel();
            }
            if (crystal.isDead()) {
                bukkitTask.cancel();
            }
        }, 0, 1);
    }

    private void circlingExplosion(Player player, int amount, ItemStack crystalStack, CrystalSource source, Location endLocation) {
        List<Item> crystals = new ArrayList<>();
        World world = player.getWorld();
        double distanceInAngles = (2 * Math.PI) / amount; // Angle increment: 360 degrees divided by the amount of crystals AKA distance between each crystal
        float widthMultiplier = 3.0f;
        plugin.getCrystalsSFX().crystalAppear.playForPlayerAtLocation(player);
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int i = 0; i < amount; i++) {
                ItemStack duplicate = crystalStack.clone();
                duplicate.setAmount(1);
                double currentAngle = distanceInAngles * i;
                Location spawnLocation = player.getLocation().clone().add(
                        Math.cos(currentAngle) * widthMultiplier,
                        0,
                        Math.sin(currentAngle) * widthMultiplier);
                Item crystal = world.dropItem(spawnLocation, duplicate);
                crystal.setCanPlayerPickup(false);
                crystal.setGravity(false);
                crystal.setVelocity(new Vector(0, 0, 0));
                crystals.add(crystal);
            }
            Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
                boolean isAnimationDone = false;
                for (int i = 0; i < crystals.size(); i++) {
                    Item crystal = crystals.get(i);
                    if (crystal.isDead()) {
                        continue;
                    }
                    Location crystalLocation = crystal.getLocation();
                    if (crystal.getTicksLived() < 200) {
                        long timeAlive = crystal.getTicksLived(); // t
                        double timeAliveMultiplier = 0.1;
                        double angleIncrement = timeAlive * timeAliveMultiplier;
                        double totalAngle = distanceInAngles * i + angleIncrement;
                        Location nextLocation = player.getLocation().clone().add(
                                Math.cos(totalAngle) * widthMultiplier / Math.max((timeAlive * timeAliveMultiplier * 0.25), 1),
                                timeAlive * timeAliveMultiplier / 6,
                                Math.sin(totalAngle) * widthMultiplier / Math.max((timeAlive * timeAliveMultiplier * 0.25), 1));
                        Vector direction = VectorUtils.getDirection(crystalLocation.toVector(), nextLocation.toVector());
                        double distance = Math.max(crystal.getLocation().distance(nextLocation), 0.01);
                        Vector velocity = direction.multiply(0.1 * distance);
                        if (isFinite(velocity)) {
                            crystal.setVelocity(velocity);
                        }
                    } else if (crystal.getTicksLived() == 200) {
                        crystal.setGravity(true);
                        // Shoot in random direction
                        Vector randomVelocity = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(0.5);
                        if (isFinite(randomVelocity)) {
                            crystal.setVelocity(randomVelocity);
                        }
                        plugin.getCrystalsSFX().circlingExplosion.playAtLocation(endLocation);
                        ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.HIDE_DROP_NAME);
                        isAnimationDone = true;
                    }
                    if (!isAnimationDone) {
                        world.spawnParticle(Particle.DOLPHIN, crystalLocation.clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                        if (crystal.getTicksLived() % 4 == 0) {
                            world.spawnParticle(Particle.FIREWORK, crystalLocation.clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                        }
                        if (crystal.getTicksLived() % 15 + plugin.random().nextInt(10) == 0) {
                            plugin.getCrystalsSFX().crystalHover.playAtLocation(crystalLocation);
                        }
                    } else {
                        crystal.setThrower(player.getUniqueId());
                        crystal.setCanPlayerPickup(true);
                        crystal.setPickupDelay(20);
                    }
                }
                if (isAnimationDone) {
                    IncrementCrystalsEvent incrementEvent = new IncrementCrystalsEvent(amount, source);
                    incrementEvent.callEvent();
                    if (incrementEvent.isCancelled()) {
                        for (Item crystal : crystals) {
                            crystal.remove();
                        }
                    }
                    bukkitTask.cancel();
                }
            }, 0, 1); // Start immediately, repeat every tick
        });
    }

    private boolean isFinite(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }

    @Override
    public void addCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> crystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);

        if (source == CrystalSource.COMMAND || source == CrystalSource.SALE) {
            IncrementCrystalsEvent event = new IncrementCrystalsEvent(amount, source);
            event.callEvent();
            if (event.isCancelled()) {
                return;
            }
            amount = event.getAmount();
        }

        if (crystals.isPresent()) {
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, crystals.get() + amount);
        } else {
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, amount);
        }
        plugin.getLogger().info("Added " + amount + " crystals to " + player.getName());
    }

    @Override
    public void setCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> oldCrystalAmount = playerData.getDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        int difference;
        if (oldCrystalAmount.isPresent()) {
            difference = amount - oldCrystalAmount.get();
        } else {
            difference = amount;
        }

        if (source == CrystalSource.COMMAND || source == CrystalSource.SALE) {
            if (difference > 0) {
                IncrementCrystalsEvent event = new IncrementCrystalsEvent(difference, source);
                event.callEvent();
                if (event.isCancelled()) {
                    return;
                }
            } else {
                DecrementCrystalsEvent event = new DecrementCrystalsEvent(Math.abs(difference), source);
                event.callEvent();
                if (event.isCancelled()) {
                    return;
                }
            }
        }
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, amount);
        plugin.getLogger().info("Set " + player.getName() + "'s crystals to " + amount);
    }

    @Override
    public void removeCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> crystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        if (crystals.isEmpty()) {
            return;
        }
        DecrementCrystalsEvent decrementEvent = new DecrementCrystalsEvent(amount, source);
        decrementEvent.callEvent();
        if (decrementEvent.isCancelled()) {
            return;
        }
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, crystals.get() - decrementEvent.getAmount());
        if (source == CrystalSource.SALE) {
            plugin.getStatistics().setCrystalsSpent(plugin.getStatistics().getCrystalsSpent() + decrementEvent.getAmount());
        } else {
            plugin.getStatistics().setCrystalsLost(plugin.getStatistics().getCrystalsLost() + decrementEvent.getAmount());
        }
        plugin.getLogger().info("Removed " + decrementEvent.getAmount() + " crystals from " + player.getName());
    }

    @Override
    public int getCrystals(Player player) {
        Optional<Integer> crystalAmount = plugin.getPlayerDataService().getData(player.getUniqueId()).getDataValue(DatabaseNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        return crystalAmount.orElse(0);
    }

}
