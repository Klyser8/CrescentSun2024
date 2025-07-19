package it.crescentsun.mobmadness;

import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.CrescentPlugin;

import javax.management.ServiceNotFoundException;

public final class MobMadness extends CrescentPlugin {


    private ArtifactRegistryService artifactRegistryService;

    @Override
    public void onEnable() {
        initServices();
    }

    @Override
    protected void initServices() {
        super.initServices();
        try {
            artifactRegistryService = getServiceProvider(ArtifactRegistryService.class);
            crescentCoreAPI = getServiceProvider(CrescentCoreAPI.class);
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
