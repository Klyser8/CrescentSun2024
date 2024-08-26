package it.crescentsun.jumpwarps.warphandling;

import it.crescentsun.jumpwarps.JumpWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class JumpWarpBlock {

    private final String jumpWarpName;
    private final Location loc;
    private final String targetServerName;
    private final JumpWarps plugin;
    private final Consumer<BukkitTask> bukkitTask;

    public JumpWarpBlock(JumpWarps plugin, String jumpWarpName, Location loc, String targetServerName) {
        this.plugin = plugin;
        this.jumpWarpName = jumpWarpName;
        this.loc = loc;
        this.targetServerName = targetServerName;
        bukkitTask = new JumpWarpScheduledTask(plugin, this);
        scheduleTask();
    }

    /**
     * Gets the name of the jumpwarp.
     * @return The name of the jumpwarp.
     */
    public String getJumpWarpName() {
        return jumpWarpName;
    }

    /**
     * Gets the location of the jumpwarp.
     * @return The location of the jumpwarp.
     */
    public Location getLocation() {
        return loc.clone();
    }

    /**
     * Gets the name of the target server the jumpwarp leads to.
     * @return The name of the target server.
     */
    public String getTargetServerName() {
        return targetServerName;
    }

    /**
     * Gets the x coordinate of the jumpwarp.
     * @return The x coordinate of the jumpwarp.
     */
    public int getX() {
        return loc.getBlockX();
    }

    /**
     * Gets the y coordinate of the jumpwarp.
     * @return The y coordinate of the jumpwarp.
     */
    public int getY() {
        return loc.getBlockY();
    }

    /**
     * Gets the z coordinate of the jumpwarp.
     * @return The z coordinate of the jumpwarp.
     */
    public int getZ() {
        return loc.getBlockZ();
    }

    /**
     * Schedules the task to run every 10 ticks.
     */
    private void scheduleTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask, 0, 5);
    }
}
