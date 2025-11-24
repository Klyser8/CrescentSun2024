package it.crescentsun.api.lumenspawn;

/**
 * Represents a class which provides Lumen Spawns.
 */
public interface LumenSpawnProvider {

    /**
     * Override this method if you would like to register new Lumen Spawns through your plugin.
     *
     * @param registryService The service used to register Lumen Spawns.
     */
    default void onArtifactRegister(LumenSpawnRegistryService registryService) {

    }

}
