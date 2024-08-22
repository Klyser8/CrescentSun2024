package it.klynet.klynetcore.api;

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

}
