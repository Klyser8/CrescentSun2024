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
import java.util.function.Consumer;

public class JumpWarpScheduledTask implements Consumer<BukkitTask> {

    private final JumpWarps plugin;
    private final JumpWarpBlock jumpWarp;
    private int tickCounter;
    private final Location fxLoc;

    public JumpWarpScheduledTask(JumpWarps plugin, JumpWarpBlock jumpWarp) {
        this.plugin = plugin;
        this.jumpWarp = jumpWarp;
        tickCounter = 0;
        fxLoc = jumpWarp.getLocation().clone().add(0.5, 0.5, 0.5);
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        if (!plugin.getJumpWarpManager().getJumpWarps().containsKey(jumpWarp.getJumpWarpName())) {
            bukkitTask.cancel();
        }
        //Check if a player enters the jumpwarp.
        Collection<Player> nearbyPlayers = jumpWarp.getLocation().add(0.5, 0, 0.5).getNearbyPlayers(0.4);
        World world = jumpWarp.getLocation().getWorld();
        if (!nearbyPlayers.isEmpty()) {
            nearbyPlayers.forEach(player -> {
                //Check if a jumpwarp is at the player's location.
                if (plugin.getJumpWarpManager().getJumpWarpAtLocation(player.getLocation()) != null) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 6, 93));
                    world.spawnParticle(org.bukkit.Particle.CRIT, fxLoc.clone().add(0, 1, 0),
                            10, 0.25, 0.5, 0.25, 0.1);
                }
            });
        } else {
            world.spawnParticle(org.bukkit.Particle.CRIT, fxLoc,
                    5, 0.25, 0.25, 0.25, 0.1);
        }
        //Spawn crit particles where the jumpwarp is.

        if (tickCounter % 3 == 0) {
            world.playSound(
                    fxLoc, Sound.BLOCK_BEACON_AMBIENT, SoundCategory.AMBIENT, 1, 2);
        }
        tickCounter++;
    }
}