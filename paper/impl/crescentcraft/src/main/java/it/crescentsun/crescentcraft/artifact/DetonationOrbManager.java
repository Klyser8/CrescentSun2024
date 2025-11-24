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
import org.bukkit.*;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DetonationOrbManager extends AbstractPluginDataManager<CrescentCraft, DetonationOrbData> implements Listener {

    private static final Duration MAX_LIFETIME = Duration.ofMinutes(5);

    private final SoundEffect placeSound;
    private final CompositeSoundEffect readySound;
    private final SoundEffect ambientSound;
    private final SoundEffect explodeSound;

    private final Multimap<UUID, PendingNotification> pendingNotifications = ArrayListMultimap.create();

    public DetonationOrbManager(CrescentCraft plugin, PluginDataService pluginDataService) {
        super(plugin, DetonationOrbData.class, pluginDataService);
        placeSound = new SoundEffect(plugin, Sound.BLOCK_CORAL_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 0.5f);
        readySound = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_MAGMA_CUBE_DEATH, SoundCategory.PLAYERS, 0.75f, 0.5f),
                new SoundEffect(plugin, Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 0.75f, 1.2f));
        ambientSound = new SoundEffect(plugin, Sound.BLOCK_FIRE_AMBIENT, SoundCategory.AMBIENT, 0.6f, 1.45f);
        explodeSound = new SoundEffect(plugin, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 1.2f);
    }

    public void placeOrb(Player owner, Location location) {
        prepareBlock(location, Material.DEAD_FIRE_CORAL_FAN);
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
        removeOrbBlock(data.getLocation());
        notifyOwner(data);
        data.deleteAndSync();
        return true;
    }

    public void startOrbTask(DetonationOrbData data) {
        plugin.getServer().getScheduler().runTask(plugin, () -> new DetonationOrbRunnable(data).runTaskTimer(plugin, 0L, 4L));
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
        String message = ChatColor.YELLOW + "The Detonation Orb found at " + data.getX() + ", " +
                data.getY() + ", " + data.getZ() + " Wilted! It will no longer work.";
        Player owner = plugin.getServer().getPlayer(data.getOwnerUuid());
        if (owner != null && owner.getWorld().getUID().equals(worldId)) {
            owner.sendMessage(message);
        } else {
            pendingNotifications.put(data.getOwnerUuid(), new PendingNotification(worldId, message));
        }
    }

    private void prepareBlock(Location location, Material material) {
        Runnable task = () -> {
            location.getBlock().setType(material);
            CoralWallFan blockData = ((CoralWallFan) location.getBlock().getBlockData());
            blockData.setWaterlogged(false);
            location.getBlock().setBlockData(blockData);
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
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
                data.deleteAndSync();
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
            removeOrbBlock(location);
            data.deleteAndSync();
            cancel();
        }
    }

    private record PendingNotification(UUID worldId, String message) { }
}
