package it.crescentsun.lumenspawn;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.lumenspawn.LumenSpawnAPI;
import it.crescentsun.api.lumenspawn.LumenSpawnRegistryService;
import it.crescentsun.api.lumenspawn.mob.LumenSpawn;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.ServicePriority;


public final class LumenSpawnMain extends CrescentPlugin implements LumenSpawnAPI {

    private LumenSpawnRegistryService lumenSpawnRegistryService;
    private LumenSpawnAPI lumenSpawnAPI;

    @Override
    public void onEnable() {
        initServices();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void spawnLumenEntity(NamespacedKey namespacedKey, Location spawnLocation) {
        LumenSpawn<? extends Entity> lumenSpawn = lumenSpawnRegistryService.getLumenSpawn(namespacedKey);
        if (lumenSpawn == null) {
            getLogger().warning("LumenSpawn with key " + namespacedKey + " not found.");
            return;
        }
        lumenSpawn.spawnEntity(spawnLocation);
    }

    @Override
    protected void initServices() {
        super.initServices();
        lumenSpawnRegistryService = new LumenSpawnRegistry(this);
        lumenSpawnAPI = this;
        serviceManager.register(LumenSpawnRegistryService.class, lumenSpawnRegistryService, this, ServicePriority.Normal);
        serviceManager.register(LumenSpawnAPI.class, lumenSpawnAPI, this, ServicePriority.Normal);
    }
}
