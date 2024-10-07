package it.crescentsun.api.crescentcore.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * A utility class for locations.
 */
public class VectorUtils {
    /**
     * Checks if two locations are within the same block.
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return True if the locations are in the same block location, false otherwise.
     */
    public static boolean isInSameBlockLocation(Location loc1, Location loc2) {
        loc1 = loc1.clone().toBlockLocation();
        loc2 = loc2.clone().toBlockLocation();
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        loc1.setY(0);
        loc2.setY(0);
        double distance = loc1.distance(loc2);
        return distance == 0;
    }

    /**
     * Checks if the two locations are within each others 'line of sight'.
     * @param from The first location.
     * @param to The second location.
     * @return True if the two locations are within each others 'line of sight', false otherwise.
     */
    public static boolean hasLineOfSight(Location from, Location to) {
        if (!from.getWorld().equals(to.getWorld())) {
            return false;
        }
        World world = from.getWorld();
        RayTraceResult rayTraceResult = world.rayTrace(
                from, to.toVector().subtract(from.toVector()).normalize(), to.distance(from),
                FluidCollisionMode.NEVER, true, 0.0, entity -> false);
        return rayTraceResult == null || rayTraceResult.getHitEntity() == null;
    }

    public static Vector getDirection(Vector from, Vector to) {
        return to.clone().subtract(from).normalize();
    }

    public static Vector getDirection(Location from, Location to) {
        return to.clone().toVector().subtract(from.toVector()).normalize();
    }

}
