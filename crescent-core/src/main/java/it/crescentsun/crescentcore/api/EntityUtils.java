package it.crescentsun.crescentcore.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class EntityUtils {

    /**
     * Returns a list of entities of a specific class that are within a certain radius of the given entity.
     *
     * @param entity the entity around which to search for other entities
     * @param radius the radius within which to search for other entities
     * @param clazz the class of the entities to search for
     * @return a list of entities that are within the specified
     *         radius of the given entity and are of the specified class
     */
    public static <T extends Entity> List<T> getNearbyEntitiesByClass(Entity entity, double radius, Class<T> clazz) {
        List<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);
        List<T> matchingEntities = new ArrayList<>();
        for (Entity nearbyEntity : nearbyEntities) {
            if (clazz.isInstance(nearbyEntity)) {
                matchingEntities.add(clazz.cast(nearbyEntity));
            }
        }
        return matchingEntities;
    }

    /**
     * Removes an entity and all of its passengers from the world.
     *
     * @param entity the entity to remove
     */
    public static void removeEntityAndPassengers(Entity entity) {
        if (!entity.getPassengers().isEmpty()) {
            for (Entity passenger : entity.getPassengers()) {
                removeEntityAndPassengers(passenger);
            }
        }
        entity.remove();
    }

    /**
     * Returns a safe location underneath the given entity. May be null if no safe location is found.
     * A location is considered safe if the block underneath the location is solid and the block itself is empty.
     *
     * @param entity the entity to find a safe location underneath
     * @return a safe location underneath the entity, or null if no safe location is found
     */
    public static Location getSafeLocationUnderneath(Entity entity) {
        for (int y = (int) entity.getY(); y > -64; y--) {
            if (!entity.getWorld().getBlockAt((int) entity.getX(), y, (int) entity.getZ()).isEmpty()) { // If the block being checked is not empty, continue
                continue;
            }
            if (!entity.getWorld().getBlockAt((int) entity.getX(), y - 1, (int) entity.getZ()).isSolid()) { // If the block underneath the block being checked is not solid, continue
                continue;
            }
            // As the block being checked is empty and the block underneath is solid, this location should be safe.
            return new Location(entity.getWorld(), entity.getX(), y, entity.getZ());
        }
        return null;
    }

}
