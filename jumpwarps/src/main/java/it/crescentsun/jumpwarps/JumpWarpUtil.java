package it.crescentsun.jumpwarps;

import org.bukkit.Location;

public class JumpWarpUtil {

    public static double JUMPWARP_DETECTION_RADIUS = 1.75;

    /**
     * Checks if the given location is within a jump warp.
     * @param loc The location to check.
     * @return True if the location is within a jump warp, false otherwise.
     */
    public static boolean isWithinJumpWarp(Location loc, JumpWarpData jumpWarp) {
        return jumpWarp.getLocation().distance(loc) < JUMPWARP_DETECTION_RADIUS;
    }

}
