package it.crescentsun.crystals;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.VectorUtils;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.api.crystals.CrystalsService;
import it.crescentsun.api.crystals.event.SpawnCrystalsEvent;
import it.crescentsun.crystals.artifact.CrystalArtifact;
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

    private static final Vector ZERO_VECTOR = new Vector(0, 0, 0);
    private final Crystals plugin;
    public CrystalManager(Crystals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnCrystals(@Nullable Player player, int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation, @Nullable Location spawnLocation) {
        if (amount > spawnAnimation.getMax() || amount < spawnAnimation.getMin()) {
            plugin.getLogger().warning("Amount of crystals to spawn is out of bounds for the animation. Defaulting to the minimum");
            amount = spawnAnimation.getMin();
        }
        Location baseLocation;
        if (spawnLocation != null) {
            baseLocation = spawnLocation.clone();
        } else {
            baseLocation = player != null ? player.getLocation() : null;
        }
        if (baseLocation == null) {
            plugin.getLogger().warning("Unable to determine crystal spawn location");
            return;
        }
        SpawnCrystalsEvent event = new SpawnCrystalsEvent(player, amount, baseLocation, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        int finalAmount = Math.min(event.getAmount(), 99);
        Location finalLocation = event.getLocation();
        if (spawnAnimation == CrystalSpawnAnimation.CIRCLING_EXPLOSION) {
            circlingExplosion(player, finalAmount, source, finalLocation.clone().add(0, 3, 0));
        } else if (spawnAnimation == CrystalSpawnAnimation.HOVER) {
            hover(player, source, finalLocation);
        } else if (spawnAnimation == CrystalSpawnAnimation.SPRING_SIMULTANEOUS) {
            springSimultaneous(player, finalAmount, source, finalLocation);
        } else if (spawnAnimation == CrystalSpawnAnimation.SPRING_SEQUENTIAL) {
            springSequential(player, finalAmount, source, finalLocation);
        }
    }

    @Override
    public void dropCrystals(@Nullable Player owner, Location location, int amount, CrystalSource dropReason) {
        if (amount < 1 || amount > 99) {
            plugin.getLogger().warning("Amount of crystals to drop is out of bounds. Defaulting to 1");
            amount = 1;
        }
        SpawnCrystalsEvent event = new SpawnCrystalsEvent(owner, amount, location, dropReason);
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }
        ItemStack crystalStack = plugin.getArtifactRegistryService()
                .getArtifact(ArtifactNamespacedKeys.CRYSTAL)
                .createStack(event.getAmount());
        Item crystalItem = location.getWorld().dropItem(location, crystalStack);
        crystalItem.setPickupDelay(10);
        if (owner != null) {
            crystalItem.setThrower(owner.getUniqueId());
        }
    }

    @Override
    public int getCrystalsSpawned(Player player) {
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> crystalsSpawned = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_SPAWNED);
        return crystalsSpawned.orElse(0);
    }

    @Override
    public int getCrystalsInVault(Player player) {
        if (player == null) {
            return 0;
        }
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> crystalsInVault = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
        return crystalsInVault.orElse(0);
    }

    @Override
    public void addCrystalsToVault(Player player, int amount) {
        if (player == null) {
            return;
        }
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> currentCrystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
        int newAmount = currentCrystals.orElse(0) + amount;
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, newAmount);
    }

    @Override
    public void removeCrystalsFromVault(Player player, int amount) {
        if (player == null) {
            return;
        }
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> currentCrystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
        int newAmount = currentCrystals.orElse(0) - amount;
        if (newAmount < 0) {
            newAmount = 0; // Prevent negative crystals
        }
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, newAmount);
    }

    @Override
    public void setCrystalsInVault(Player player, int amount) {
        if (player == null) {
            return;
        }
        PlayerData playerData = plugin.getPlayerDataService().getData(player.getUniqueId());
        if (amount < 0) {
            amount = 0; // Prevent negative crystals
        }
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, amount);
    }

    private void hover(@Nullable Player player, CrystalSource source, @Nullable Location spawnLocation) {
        Location baseLocation = spawnLocation != null ? spawnLocation.clone() : (player != null ? player.getLocation() : null);
        if (baseLocation == null) {
            plugin.getLogger().warning("Unable to determine crystal spawn location for hover animation");
            return;
        }
        CrystalArtifact crystalArtifact = (CrystalArtifact) plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL);
        ItemStack blueprintCrystal = crystalArtifact.createStack(1);
        ArtifactUtil.addFlagsToStack(blueprintCrystal, ArtifactFlag.HIDE_DROP_NAME);
        AtomicInteger movementTicks = new AtomicInteger();
        // item entity should stay in place for about two seconds, then move 50% of the way between it and the player who won it.
        AtomicReference<Location> previousTargetLoc = new AtomicReference<>(player != null ? player.getLocation() : baseLocation);
        Item crystal = baseLocation.getWorld().dropItem(baseLocation, blueprintCrystal);
        crystal.setGravity(false);
        crystal.setVelocity(ZERO_VECTOR.clone());
        crystal.setCanPlayerPickup(false);
        World world = crystal.getWorld();
        plugin.getCrystalsSFX().crystalAppear.playAtLocation(baseLocation);
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
            int ticksLived = crystal.getTicksLived();
            if (ticksLived < 40) {
                // Reset velocity and position to keep the crystal stationary
                crystal.setVelocity(ZERO_VECTOR.clone());
                //TODO: refactor this mess
                world.spawnParticle(Particle.DOLPHIN, crystal.getLocation().clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                if (crystal.getTicksLived() % 4 == 0) {
                    world.spawnParticle(Particle.FIREWORK, crystal.getLocation().clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                }
            }
            if (ticksLived >= 40) {
                movementTicks.incrementAndGet();
                // Apply velocity so that it slowly goes towards the player, for the next 20 seconds - slowing down as it gets closer.
                Location targetLoc = player != null ? player.getLocation().clone().add(0, 1, 0) : baseLocation;
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
                SpawnCrystalsEvent incrementEvent = new SpawnCrystalsEvent(player, blueprintCrystal.getAmount(), crystal.getLocation(), source);
                incrementEvent.callEvent();
                if (incrementEvent.isCancelled()) {
                    crystal.remove();
                    bukkitTask.cancel();
                    return;
                }

                crystal.setGravity(true);
                crystal.setPickupDelay(20);
                crystal.setCanPlayerPickup(true);
                if (player != null) {
                    crystal.setThrower(player.getUniqueId());
                }
                ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.HIDE_DROP_NAME);

                bukkitTask.cancel();
            }
            if (crystal.isDead()) {
                bukkitTask.cancel();
            }
        }, 0, 1);
    }

    private void springSimultaneous(@Nullable Player player, int amount, CrystalSource source, Location location) {
        CrystalArtifact crystalArtifact = (CrystalArtifact) plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL);
        ItemStack blueprintCrystal = crystalArtifact.createStack(1);
        ArtifactUtil.addFlagsToStack(blueprintCrystal, ArtifactFlag.HIDE_DROP_NAME);
        World world = location.getWorld();
        List<Item> crystals = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            ItemStack single = blueprintCrystal.clone();
            ArtifactUtil.addFlagsToStack(single, ArtifactFlag.UNIQUE);
            Item crystal = world.dropItem(location.clone(), single);
            crystal.setCanPlayerPickup(false);
            crystal.setGravity(false);
            crystal.setVelocity(ZERO_VECTOR.clone());
            crystals.add(crystal);
        }

        plugin.getCrystalsSFX().crystalAppear.playAtLocation(location);

        AtomicInteger ticks = new AtomicInteger();
        double angleStep = (2 * Math.PI) / amount;

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            int t = ticks.getAndIncrement();
            if (t < 20) {
                crystals.forEach(item -> {
                    if (item.isValid()) item.setVelocity(new Vector(0, 0.05, 0));
                });
            } else if (t == 20) {
                for (int i = 0; i < crystals.size(); i++) {
                    Item item = crystals.get(i);
                    if (!item.isValid()) continue;
                    double angle = angleStep * i;
                    Vector launch = new Vector(Math.cos(angle), 0.35, Math.sin(angle)).multiply(0.5);
                    if (isFinite(launch)) item.setVelocity(launch);
                    item.setGravity(true);
                    item.setCanPlayerPickup(true);
                    item.setPickupDelay(20);
                    if (player != null) item.setThrower(player.getUniqueId());
                    item.setTicksLived(1);
                }

                // strip flags now, before any pickups happen
                crystals.forEach(drop -> {
                    ArtifactUtil.removeFlagsFromItem(drop, ArtifactFlag.HIDE_DROP_NAME);
                    ArtifactUtil.removeFlagsFromItem(drop, ArtifactFlag.UNIQUE);
                });

                SpawnCrystalsEvent spawnCrystalsEvent = new SpawnCrystalsEvent(player, amount, location, source);
                spawnCrystalsEvent.callEvent();
                if (spawnCrystalsEvent.isCancelled()) {
                    crystals.forEach(Item::remove);
                    task.cancel();
                }
            } else if (t > 60 || crystals.stream().allMatch(Item::isDead)) {
                task.cancel();
            }
        }, 0, 1);
    }

    private void springSequential(@Nullable Player player, int amount, CrystalSource source, Location location) {
        CrystalArtifact crystalArtifact = (CrystalArtifact) plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL);
        ItemStack blueprintCrystal = crystalArtifact.createStack(1);
        ArtifactUtil.addFlagsToStack(blueprintCrystal, ArtifactFlag.HIDE_DROP_NAME);
        ArtifactUtil.addFlagsToStack(blueprintCrystal, ArtifactFlag.UNIQUE);
        World world = location.getWorld();

        int totalTicks = 60;
        int delay = Math.max(1, totalTicks / amount);

        for (int i = 0; i < amount; i++) {
            int tickDelay = delay * i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack single = blueprintCrystal.clone();
                Item crystal = world.dropItem(location.clone(), single);
                crystal.setCanPlayerPickup(false);
                crystal.setPickupDelay(20);
                Vector dir = new Vector(plugin.random().nextDouble() - 0.5, 0.6 + plugin.random().nextDouble() * 0.2, plugin.random().nextDouble() - 0.5).multiply(0.5);
                if (isFinite(dir)) crystal.setVelocity(dir);
                if (player != null) crystal.setThrower(player.getUniqueId());

                plugin.getCrystalsSFX().crystalAppear.playAtLocation(location);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    crystal.setCanPlayerPickup(true);
                    ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.HIDE_DROP_NAME);
                    ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.UNIQUE);
                }, 20);

                SpawnCrystalsEvent spawnCrystalsEvent = new SpawnCrystalsEvent(player, 1, location, source);
                spawnCrystalsEvent.callEvent();
                if (spawnCrystalsEvent.isCancelled()) {
                    crystal.remove();
                }
            }, tickDelay);
        }
    }

    private void circlingExplosion(@Nullable Player player, int amount, CrystalSource source, Location endLocation) {
        if (endLocation == null) {
            plugin.getLogger().warning("End location is required for circling explosion animation");
            return;
        }
        CrystalArtifact crystalArtifact = (CrystalArtifact) plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL);
        ItemStack blueprintCrystal = crystalArtifact.createStack(1);
        ArtifactUtil.addFlagsToStack(blueprintCrystal, ArtifactFlag.HIDE_DROP_NAME);
        List<Item> crystals = new ArrayList<>();
        World world = endLocation.getWorld();
        double angleStep = (2 * Math.PI) / amount;
        float radius = 3.0f;

        // Determine center point
        Location centerLocation = player != null
                ? player.getLocation().clone()
                : endLocation.clone();

        plugin.getCrystalsSFX().crystalAppear.playAtLocation(endLocation);

        Bukkit.getScheduler().runTask(plugin, () -> {
            // Initial spawn
            for (int i = 0; i < amount; i++) {
                ItemStack single = blueprintCrystal.clone();
                double angle = angleStep * i;
                Location spawnLoc = centerLocation.clone().add(
                        Math.cos(angle) * radius,
                        0,
                        Math.sin(angle) * radius
                );
                Item crystal = world.dropItem(spawnLoc, single);
                crystal.setCanPlayerPickup(false);
                crystal.setGravity(false);
                crystal.setVelocity(ZERO_VECTOR.clone());
                crystals.add(crystal);
            }

            // Animation loop
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                boolean done = false;
                for (int i = 0; i < crystals.size(); i++) {
                    Item crystal = crystals.get(i);
                    if (crystal.isDead()) continue;
                    long ticks = crystal.getTicksLived();
                    Location loc = crystal.getLocation();

                    if (ticks < 200) {
                        double tMul = 0.1 * ticks;
                        double totalAngle = angleStep * i + tMul;
                        Location target = centerLocation.clone().add(
                                Math.cos(totalAngle) * (radius / Math.max(tMul * 0.25, 1)),
                                tMul / 6,
                                Math.sin(totalAngle) * (radius / Math.max(tMul * 0.25, 1))
                        );
                        Vector dir = VectorUtils.getDirection(loc.toVector(), target.toVector());
                        double dist = Math.max(loc.distance(target), 0.01);
                        Vector vel = dir.multiply(0.1 * dist);
                        if (isFinite(vel)) crystal.setVelocity(vel);

                        world.spawnParticle(Particle.DOLPHIN, loc.clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                        if (ticks % 4 == 0) {
                            world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                        }
                        if (ticks % 15 + plugin.random().nextInt(10) == 0) {
                            plugin.getCrystalsSFX().crystalHover.playAtLocation(loc);
                        }

                    } else if (ticks == 200) {
                        crystal.setGravity(true);
                        Vector rand = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(0.5);
                        if (isFinite(rand)) crystal.setVelocity(rand);
                        plugin.getCrystalsSFX().circlingExplosion.playAtLocation(endLocation);
                        ArtifactUtil.removeFlagsFromItem(crystal, ArtifactFlag.HIDE_DROP_NAME);
                        if (player != null) {
                            crystal.setThrower(player.getUniqueId());
                        }
                        crystal.setCanPlayerPickup(true);
                        crystal.setPickupDelay(20);
                        crystal.setTicksLived(1);
                        done = true;
                    }
                }

                if (done) {
                    SpawnCrystalsEvent spawnCrystalsEvent = new SpawnCrystalsEvent(player, amount, endLocation, source);
                    spawnCrystalsEvent.callEvent();
                    if (spawnCrystalsEvent.isCancelled()) {
                        crystals.forEach(Item::remove);
                    }
                    task.cancel();
                }
            }, 0, 1);
        });
    }

    private static boolean isFinite(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }

}
