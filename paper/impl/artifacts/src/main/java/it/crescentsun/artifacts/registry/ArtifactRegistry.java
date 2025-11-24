package it.crescentsun.artifacts.registry;

import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.artifacts.item.Artifact;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArtifactRegistry implements ArtifactRegistryService {

    private final Map<NamespacedKey, Artifact> registeredArtifacts = new HashMap<>();

    @Override
    public void registerArtifact(Artifact artifact) {
        Bukkit.getLogger().info("Registering artifact: " + artifact.namespacedKey());
        registeredArtifacts.put(artifact.namespacedKey(), artifact);
    }

    @Override
    public Artifact getArtifact(NamespacedKey namespacedKey) {
        return registeredArtifacts.get(namespacedKey);
    }

    @Override
    public Collection<Artifact> getRegisteredArtifacts() {
        return registeredArtifacts.values();
    }
}
