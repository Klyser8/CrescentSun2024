package it.crescentsun.api.artifacts;

import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import org.bukkit.NamespacedKey;

import java.util.Collection;

/**
 * Service for registering artifacts.
 */
public interface ArtifactRegistryService {

    /**
     * Registers an artifact with the given namespaced key. Optimally, the namespaced key should be
     * written inside {@link ArtifactNamespacedKeys}, to make fetching the artifact easier for other plugins.
     *
     * @param artifact The artifact to register.
     */
    void registerArtifact(Artifact artifact);

    /**
     * Gets the artifact with the given namespaced key.
     * @param namespacedKey The namespaced key of the artifact.
     */
    Artifact getArtifact(NamespacedKey namespacedKey);

    /**
     * Gets all registered artifacts.
     * @return All registered artifacts.
     */
    Collection<Artifact> getRegisteredArtifacts();

}
