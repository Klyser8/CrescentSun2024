package it.crescentsun.api.lumenspawn;

import it.crescentsun.api.lumenspawn.mob.LumenSpawn;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;

import java.util.Collection;

public interface LumenSpawnRegistryService {

    /**
     * Registers a LumenSpawn with the given namespaced key.
     *
     * @param lumenSpawn The LumenSpawn to register.
     */
    void registerLumenSpawn(LumenSpawn<? extends Entity> lumenSpawn);

    /**
     * Gets the LumenSpawn with the given namespaced key.
     * @param namespacedKey The namespaced key of the LumenSpawn.
     */
    LumenSpawn<? extends Entity> getLumenSpawn(NamespacedKey namespacedKey);

    /**
     * Gets all registered LumenSpawns.
     * @return All registered LumenSpawns.
     */
    Collection<LumenSpawn<? extends Entity>> getRegisteredLumenSpawns();

}
