package it.crescentsun.api.artifacts;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

/**
 * The main API interface for Artifacts.
 * Here are miscellaneous methods that may be used to interact with the plugin.
 */
public interface ArtifactsAPI {

    /**
     * Creates a default item stack of the artifact with the given namespaced key, with the given amount.
     * @param namespacedKey The namespaced key of the artifact.
     * @param amount The amount of the item stack.
     * @return The item stack of the artifact.
     */
    ItemStack createArtifactStack(NamespacedKey namespacedKey, int amount);

    /**
     * Creates a default item stack of the artifact with the given namespaced key.
     * @param namespacedKey The namespaced key of the artifact.
     * @return The item stack of the artifact.
     */
    default ItemStack createArtifactStack(NamespacedKey namespacedKey) {
        return createArtifactStack(namespacedKey, 1);
    }

}
