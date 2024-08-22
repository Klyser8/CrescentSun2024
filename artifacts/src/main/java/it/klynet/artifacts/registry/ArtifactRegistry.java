package it.klynet.artifacts.registry;

import it.klynet.artifacts.item.Artifact;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

public class ArtifactRegistry {

    public static final Map<NamespacedKey, Artifact> ARTIFACTS = new HashMap<>();

    public static void registerArtifact(NamespacedKey key, Artifact artifact) {
        Bukkit.getLogger().info("Registering artifact: " + artifact.namespacedKey());
        ARTIFACTS.put(key, artifact);
    }

}
