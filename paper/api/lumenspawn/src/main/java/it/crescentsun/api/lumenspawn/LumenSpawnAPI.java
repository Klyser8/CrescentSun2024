package it.crescentsun.api.lumenspawn;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

public interface LumenSpawnAPI {

    void spawnLumenEntity(NamespacedKey namespacedKey, Location spawnLocation);

}
