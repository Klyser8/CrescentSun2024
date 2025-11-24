package it.crescentsun.crescentcraft.artifact;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.crescentsun.api.crescentcore.data.plugin.AbstractPluginDataManager;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.api.crescentcore.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.api.crescentcore.sound.CompositeSoundEffect;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import it.crescentsun.crescentcraft.CrescentCraft;
import it.crescentsun.crescentcraft.artifact.data.DetonationOrbData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DetonationOrbManager extends AbstractPluginDataManager<CrescentCraft, DetonationOrbData> implements Listener {

    private static final Duration MAX_LIFETIME = Duration.ofMinutes(5);

    private final SoundEffect placeSound;
    private final CompositeSoundEffect readySound;
    private final SoundEffect ambientSound;
    private final SoundEffect explodeSound;

    private final Multimap<UUID, PendingNotification> pendingNotifications = ArrayListMultimap.create();
    private final Map<UUID, BukkitTask> orbTasks = new HashMap<>();

    public DetonationOrbManager(CrescentCraft plugin, PluginDataService pluginDataService) {
        super(plugin, DetonationOrbData.class, pluginDataService);
        placeSound = new SoundEffect(plugin, Sound.BLOCK_CORAL_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 0.5f);
        readySound = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_MAGMA_CUBE_DEATH, SoundCategory.PLAYERS, 0.75f, 0.5f),
                new SoundEffect(plugin, Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 0.75f, 1.2f));
        ambientSound = new SoundEffect(plugin, Sound.BLOCK_FIRE_AMBIENT, SoundCategory.AMBIENT, 0.6f, 1.45f);
        explodeSound = new SoundEffect(plugin, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 1.2f);
    }

    public void placeOrb(Player owner, Location location, boolean wallPlacement, BlockFace facing) {
        prepareBlock(location, Material.DEAD_FIRE_CORAL_FAN, wallPlacement, facing);
        placeSound.playAtLocation(location);
        ambientSound.playAtLocation(location);
        DetonationOrbData data = new DetonationOrbData(
                UUID.randomUUID(),
                owner.getUniqueId(),
                plugin.getCrescentCoreAPI().getServerName(),
                location,
                System.currentTimeMillis()
        );
        data.tryInit();
        data.saveAndSync();
    }

    public boolean handleExpiry(DetonationOrbData data) {
        if (System.currentTimeMillis() - data.getPlacedAt() < MAX_LIFETIME.toMillis()) {
            return false;
        }
        deleteOrbData(data, true);
        notifyOwner(data);
        return true;
    }

    public void startOrbTask(DetonationOrbData data) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            cancelOrbTask(data.getUuid());
            DetonationOrbRunnable runnable = new DetonationOrbRunnable(data);
            BukkitTask task = runnable.runTaskTimer(plugin, 0L, 4L);
            orbTasks.put(data.getUuid(), task);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEventPostDBLoad event) {
        deliverNotifications(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        deliverNotifications(event.getPlayer());
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.FIRE_CORAL_FAN) {
            // If the block's location matches a Detonation Orb, cancel the fade
            Location loc = event.getBlock().getLocation();
            getAllData(true).stream()
                    .filter(d -> d.getWorldUuid().equals(loc.getWorld().getUID()))
                    .filter(d -> d.getX() == loc.getBlockX())
                    .filter(d -> d.getY() == loc.getBlockY())
                    .filter(d -> d.getZ() == loc.getBlockZ())
                    .findFirst().ifPresent(data -> event.setCancelled(true));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isOrbBlock(block.getType())) {
            return;
        }

        Location location = block.getLocation();
        getAllData(true).stream()
                .filter(d -> d.getWorldUuid().equals(location.getWorld().getUID()))
                .filter(d -> d.getX() == location.getBlockX())
                .filter(d -> d.getY() == location.getBlockY())
                .filter(d -> d.getZ() == location.getBlockZ())
                .findFirst()
                .ifPresent(this::deleteOrbData);
    }

    private void deliverNotifications(Player player) {
        List<PendingNotification> notifications = new ArrayList<>(pendingNotifications.get(player.getUniqueId()));
        if (notifications.isEmpty()) {
            return;
        }
        for (PendingNotification notification : notifications) {
            if (player.getWorld().getUID().equals(notification.worldId())) {
                player.sendMessage(notification.message());
                pendingNotifications.remove(player.getUniqueId(), notification);
            }
        }
    }

    private void notifyOwner(DetonationOrbData data) {
        UUID worldId = data.getWorldUuid();
        // Message to notify owner that the orb wilted. Gray, "Detonation Orb" in red, should state the coordinates of where the orb was.
        TextComponent notification = Component.text()
                .append(Component.text("Your ", NamedTextColor.GRAY))
                .append(Component.text("Detonation Orb", NamedTextColor.RED))
                .append(Component.text(" at ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("(%d, %d, %d)", data.getX(), data.getY(), data.getZ()),
                        NamedTextColor.YELLOW))
                .append(Component.text(" has wilted and will not detonate.", NamedTextColor.GRAY))
                .build();
        Player owner = plugin.getServer().getPlayer(data.getOwnerUuid());
        if (owner != null && owner.getWorld().getUID().equals(worldId)) {
            owner.sendMessage(notification);
        } else {
            pendingNotifications.put(data.getOwnerUuid(), new PendingNotification(worldId, notification));
        }
    }

    private void prepareBlock(Location location, Material material) {
        BlockData blockData = location.getBlock().getBlockData();
        boolean wallPlacement = blockData instanceof CoralWallFan;
        BlockFace facing = wallPlacement ? ((CoralWallFan) blockData).getFacing() : null;
        prepareBlock(location, material, wallPlacement, facing);
    }

    private void prepareBlock(Location location, Material material, boolean wallPlacement, BlockFace facing) {
        Runnable task = () -> {
            if (location.getWorld() == null) {
                return;
            }
            Block block = location.getBlock();
            block.setType(material);
            BlockData blockData = block.getBlockData();
            if (blockData instanceof Waterlogged waterlogged) {
                waterlogged.setWaterlogged(false);
                blockData = waterlogged;
            }
            block.setBlockData(blockData);
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    private Material resolveMaterial(Material material, boolean wallPlacement) {
        if (!wallPlacement) {
            return switch (material) {
                case FIRE_CORAL_WALL_FAN -> Material.FIRE_CORAL_FAN;
                case DEAD_FIRE_CORAL_WALL_FAN -> Material.DEAD_FIRE_CORAL_FAN;
                default -> material;
            };
        }
        return switch (material) {
            case FIRE_CORAL_FAN -> Material.FIRE_CORAL_WALL_FAN;
            case DEAD_FIRE_CORAL_FAN -> Material.DEAD_FIRE_CORAL_WALL_FAN;
            default -> material;
        };
    }

    private void removeOrbBlock(Location location) {
        if (location.getWorld() == null) {
            return;
        }
        Runnable task = () -> location.getBlock().setType(Material.AIR);
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    private void cancelOrbTask(UUID uuid) {
        BukkitTask task = orbTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void deleteOrbData(DetonationOrbData data) {
        deleteOrbData(data, false);
    }

    private void deleteOrbData(DetonationOrbData data, boolean removeBlock) {
        cancelOrbTask(data.getUuid());
        if (removeBlock) {
            removeOrbBlock(data.getLocation());
        }
        data.deleteAndSync();
    }

    private boolean isOrbBlock(Material material) {
        return material == Material.FIRE_CORAL_FAN || material == Material.DEAD_FIRE_CORAL_FAN;
    }

    private class DetonationOrbRunnable extends BukkitRunnable {

        private final DetonationOrbData data;
        private final Location location;
        private final World world;

        int clock;

        public DetonationOrbRunnable(DetonationOrbData data) {
            this.data = data;
            this.location = data.getLocation();
            this.world = location.getWorld();
            long elapsed = Math.max(0, System.currentTimeMillis() - data.getPlacedAt());
            int elapsedTicks = (int) (elapsed / 200L);
            this.clock = Math.min(elapsedTicks, 9);
            if (clock >= 9 && world != null) {
                prepareBlock(location, Material.FIRE_CORAL_FAN);
            }
        }

        @Override
        public void run() {
            if (world == null) {
                deleteOrbData(data);
                cancel();
                return;
            }
            clock++;
            playLavaParticles();
            readyTrap();
            if (clock > 10) {

                playFlameParticles();
                playAmbientFire();
                List<LivingEntity> nearbyEntities = getEntitiesNearTrap();
                if (!nearbyEntities.isEmpty()) {
                    triggerTrap(nearbyEntities);
                    remove();
                }
            }
        }

        private void playLavaParticles() {
            if (clock < 10) {
                world.spawnParticle(Particle.LAVA, location.clone().add(0.5, 0, 0.5), 3,
                        0, 0, 0, 0, null, true);
            }
        }

        private void readyTrap() {
            if (clock == 10) {
                prepareBlock(location, Material.FIRE_CORAL_FAN);
                readySound.playAtLocation(location);
            }
        }

        private void playFlameParticles() {
            world.spawnParticle(Particle.FLAME, location.clone().add(0.5, 0, 0.5), 3,
                    0.25, 0.1, 0.25, 0, null, true);
        }

        private void playAmbientFire() {
            if (plugin.random().nextInt(10) == 0) {
                ambientSound.playAtLocation(location);
            }
        }

        private List<LivingEntity> getEntitiesNearTrap() {
            List<LivingEntity> nearbyEntities = new ArrayList<>();
            for (Entity entity : location.getNearbyEntities(DetonationOrb.RADIUS, DetonationOrb.RADIUS, DetonationOrb.RADIUS)) {
                if (!(entity instanceof LivingEntity victim) || entity.getUniqueId().equals(data.getOwnerUuid())) continue;
                if (victim.hasLineOfSight(location)) {
                    nearbyEntities.add(victim);
                }
            }
            return nearbyEntities;
        }

        private void triggerTrap(List<LivingEntity> nearbyEntities) {
            explodeSound.playAtLocation(location);
            world.spawnParticle(Particle.LAVA, location.clone().add(0.5, 0, 0.5), 10,
                    1, 1, 1, 0, null, true);
            world.spawnParticle(Particle.EXPLOSION, location.clone().add(0.5, 0, 0.5),
                    5, 1, 1, 1, 0, null, true);
            world.spawnParticle(Particle.FLAME, location.clone().add(0.5, 0, 0.5), 25,
                    1, 1, 1, 0.25, null, true);
            for (LivingEntity entity : nearbyEntities) {
                entity.damage(DetonationOrb.DAMAGE, plugin.getServer().getPlayer(data.getOwnerUuid()));
                entity.setFireTicks(DetonationOrb.FIRE_TICKS);
                Vector unitVector = entity.getLocation().toVector().subtract(location.clone().add(0, -1, 0).toVector()).normalize();
                entity.setVelocity(unitVector.multiply(1.1 - (location.distance(location) / 4)));
            }
        }

        private void remove() {
            deleteOrbData(data, true);
            cancel();
        }
    }

    private record PendingNotification(UUID worldId, TextComponent message) { }
}
