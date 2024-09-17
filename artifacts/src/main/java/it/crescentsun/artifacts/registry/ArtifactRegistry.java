package it.crescentsun.artifacts.registry;

import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.crescentcore.api.registry.ArtifactNamespacedKeys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

public class ArtifactRegistry {

    public static final Map<NamespacedKey, Artifact> ARTIFACTS = new HashMap<>();

    /**
     * Registers an artifact with the given namespaced key. Optimally, the namespaced key should be
     * written inside {@link ArtifactNamespacedKeys}, to make fetching the artifact easier for other plugins.
     *
     * @param key The namespaced key of the artifact.
     * @param artifact The artifact to register.
     */
    public static void registerArtifact(NamespacedKey key, Artifact artifact) {
        Bukkit.getLogger().info("Registering artifact: " + artifact.namespacedKey());
        ARTIFACTS.put(key, artifact);
    }

}
