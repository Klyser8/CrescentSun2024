package it.crescentsun.api.artifacts;

/**
 * Represents a class which provides artifacts.
 */
public interface ArtifactProvider {

    /**
     * Override this method if you would like to register new artifacts through your plugin.
     *
     * @param registryService The service used to register artifacts.
     */
    default void onArtifactRegister(ArtifactRegistryService registryService) {

    }

}
