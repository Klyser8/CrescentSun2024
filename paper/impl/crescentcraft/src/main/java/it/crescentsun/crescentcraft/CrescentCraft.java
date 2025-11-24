package it.crescentsun.crescentcraft;

import it.crescentsun.api.artifacts.ArtifactProvider;
import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.crescentcraft.artifact.DetonationOrb;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrescentCraft extends CrescentPlugin implements ArtifactProvider {

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onArtifactRegister(ArtifactRegistryService registryService) {
        registryService.registerArtifact(new DetonationOrb(this));
    }
}
