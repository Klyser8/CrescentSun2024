package it.crescentsun.lumenspawn;

import it.crescentsun.api.lumenspawn.LumenSpawnRegistryService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.List;

public class LumenSpawnRegistry implements LumenSpawnRegistryService {

    private final LumenSpawnMain lumenSpawn;
    public LumenSpawnRegistry(LumenSpawnMain lumenSpawn) {
        this.lumenSpawn = lumenSpawn;
    }

    @Override
    public void registerLumenSpawn(it.crescentsun.api.lumenspawn.mob.LumenSpawn<? extends Entity> lumenSpawn) {

    }

    @Override
    public it.crescentsun.api.lumenspawn.mob.LumenSpawn<? extends Entity> getLumenSpawn(NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public Collection<it.crescentsun.api.lumenspawn.mob.LumenSpawn<? extends Entity>> getRegisteredLumenSpawns() {
        return List.of();
    }
}
