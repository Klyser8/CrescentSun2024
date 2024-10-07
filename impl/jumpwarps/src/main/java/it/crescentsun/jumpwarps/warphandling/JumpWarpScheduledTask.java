package it.crescentsun.jumpwarps.warphandling;

import it.crescentsun.jumpwarps.JumpWarps;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JumpWarpScheduledTask implements Consumer<BukkitTask> {

    private final JumpWarps plugin;
    private final JumpWarpManager jumpWarpManager;
    private final JumpWarpData jumpWarp;
    private int tickCounter;
    private final Location fxLoc;

    public JumpWarpScheduledTask(JumpWarps plugin, JumpWarpData jumpWarp) {
        this.plugin = plugin;
        this.jumpWarp = jumpWarp;
        this.jumpWarpManager = plugin.getJumpWarpManager();
        tickCounter = 0;
        fxLoc = jumpWarp.getLocation().clone().add(0.5, 0.5, 0.5);
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        // If the jumpwarp is removed, cancel the task.
        if (jumpWarpManager.getJumpWarpByName(jumpWarp.getWarpName()) == null) {
            bukkitTask.cancel();
        }
        //Check if a player enters the jumpwarp.
        World world = jumpWarp.getLocation().getWorld();

        world.spawnParticle(org.bukkit.Particle.CRIT, fxLoc, 5, 0.25, 0.25, 0.25, 0.1);

        Map<Player, JumpWarpManager.PlayerJumpWarpBufferEntry> buffer = jumpWarpManager.getJumpWarpBuffer();
        for (Player player : buffer.keySet()) {
            JumpWarpManager.PlayerJumpWarpBufferEntry bufferEntry = buffer.get(player);
            if (!bufferEntry.jumpWarp().equals(jumpWarp)) {
                continue;
            }

            // If current world time is more than 10 ticks ahead of the world time stored, remove the player from the map.
            if (world.getFullTime() - bufferEntry.time() > 10) {
                buffer.remove(player);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3, 93));
                world.spawnParticle(org.bukkit.Particle.CRIT, fxLoc.clone().add(0, 1, 0),
                        10, 0.25, 0.5, 0.25, 0.1);
            }
        }
        if (tickCounter % 3 == 0) {
            world.playSound(
                    fxLoc, Sound.BLOCK_BEACON_AMBIENT, SoundCategory.AMBIENT, 1, 2);
        }
        tickCounter++;
    }
}