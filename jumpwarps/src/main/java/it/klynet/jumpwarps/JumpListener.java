package it.klynet.jumpwarps;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import it.klynet.jumpwarps.warphandling.JumpWarpBlock;
import it.klynet.klynetcore.api.BungeeUtils;
import it.klynet.klynetcore.api.PlayerUtils;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import it.klynet.klynetcore.core.data.player.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class JumpListener implements Listener {

    private final JumpWarps plugin;

    public JumpListener(JumpWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRedstoneTrigger(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        JumpWarpBlock jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(loc);
        if (jumpWarp == null) {
            return;
        }
        if (!JumpWarpUtil.isWithinJumpWarp(loc, jumpWarp)) {
            return;
        }
        Location fxLoc = block.getLocation().clone().add(0.5, 0.5, 0.5);
        World world = loc.getWorld();
        if (plugin.getJumpWarpManager().getJumpWarpAtLocation(loc) == null) {
            return;
        }
        if (event.getOldCurrent() == 0 && event.getNewCurrent() > 0) {
            world.playSound(fxLoc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 2);
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();

        PlayerData data = PlayerUtils.getPlayerData(player);
        int jumps = data.getData(KlyNetNamespaceKeys.JUMPWARPS_USED);
        data.updateData(KlyNetNamespaceKeys.JUMPWARPS_USED, jumps + 1);

        JumpWarpBlock jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(player.getLocation());
        PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpWarp == null) {
            return;
        }
        if (jumpEffect == null || jumpEffect.getAmplifier() < 90) {
            return;
        }
        if (JumpWarpUtil.isWithinJumpWarp(player.getLocation(), jumpWarp)) {
            // Player is within the jump warp, so we can launch them.
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 6, 93));
            launchPlayer(player, jumpWarp);
        }
    }

    private void launchPlayer(Player player, JumpWarpBlock jumpWarpBlock) {
        World world = player.getWorld();
        playLaunchSounds(player, world);
        applyLaunchPotionEffects(player);
        scheduleLaunchTask(player, jumpWarpBlock);
    }

    private void playLaunchSounds(Player player, World world) {
        world.playSound(player.getLocation().add(0, 16, 0),
                Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2, 2);
        player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 100, 2);
        player.playSound(player, Sound.ITEM_ELYTRA_FLYING, SoundCategory.BLOCKS, 0.75f, 1.5f);
    }

    private void applyLaunchPotionEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 100, 255, false, false, false));
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.DARKNESS, 100, 255, false, false, false));
    }

    /**
     * Schedules a task that will teleport the player to the appropriate server after 2 seconds.
     * @param player The player to teleport.
     */
    private void scheduleLaunchTask(Player player, JumpWarpBlock jumpWarp) {
        World world = player.getWorld();
        new BukkitRunnable() {
            private int ticks = 0;

            private CompletableFuture<PlayerData> playerDataFut;

            @Override
            public void run() {
                if (ticks == 0) {
                    //Saves the player data.
                    plugin.getLogger().info("Saving player data...");
                    playerDataFut = plugin.getKlyNetCore().getPlayerManager().asyncSaveData(player.getUniqueId());
                    playerDataFut.thenAcceptAsync(playerData -> {
                        if (playerData != null) {
                            plugin.getLogger().info("Player data saved.");
                        }
                    });
                }
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                //After 30 ticks, will attempt sending the player to the target server every tick.
                if (ticks >= 30) {
                    plugin.getLogger().info("Attempting to send player to server...");
                    if (playerDataFut.isDone() && !playerDataFut.isCompletedExceptionally()) {
                        plugin.getLogger().info("Sending player to server.");
                        BungeeUtils.saveDataAndSendPlayerToServer(
                                plugin.getKlyNetCore(), plugin, player, jumpWarp.getTargetServerName());
                        cancel();
                    }
                }
                if (player.getVelocity().getY() > 1) {
                    spawnParticles(world, player);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 10, 1);
    }

    private void spawnParticles(World world, Player player) {
        world.spawnParticle(Particle.END_ROD, player.getLocation().add(player.getVelocity()),
                10, 0.25, 0.5, 0.25, 0);
        world.spawnParticle(Particle.CLOUD, player.getLocation().add(player.getVelocity()),
                10, 0.25, 0.5, 0.25, 0);
    }

}
