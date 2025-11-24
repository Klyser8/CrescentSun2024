package it.crescentsun.crescentcraft;

import it.crescentsun.api.artifacts.ArtifactProvider;
import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.crescentcraft.artifact.DetonationOrb;
import it.crescentsun.crescentcraft.artifact.DetonationOrbManager;
import it.crescentsun.crescentcraft.artifact.data.DetonationOrbData;

import javax.management.ServiceNotFoundException;

public final class CrescentCraft extends CrescentPlugin implements ArtifactProvider {

    private DetonationOrbManager detonationOrbManager;

    @Override
    public void onEnable() {
        initServices();
        detonationOrbManager = new DetonationOrbManager(this, pluginDataService);
        getServer().getPluginManager().registerEvents(detonationOrbManager, this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onArtifactRegister(ArtifactRegistryService registryService) {
        registryService.registerArtifact(new DetonationOrb(this));
    }

    @Override
    public void onPluginDataRegister(PluginDataRegistryService service) {
        service.registerDataClass(this, DetonationOrbData.class);
    }

    public DetonationOrbManager getDetonationOrbManager() {
        return detonationOrbManager;
    }

    @Override
    protected void initServices() {
        try {
            pluginDataService = getServiceProvider(PluginDataService.class);
            crescentCoreAPI = getServiceProvider(CrescentCoreAPI.class);
        } catch (ServiceNotFoundException e) {
            getLogger().severe("Service initialization failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
