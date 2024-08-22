package it.klynet.artifacts.api;

import it.klynet.artifacts.Artifacts;
import it.klynet.artifacts.item.Artifact;
import it.klynet.artifacts.item.AsyncArtifact;
import it.klynet.artifacts.registry.ArtifactRegistry;
import it.klynet.klynetcore.api.registry.ArtifactNamespaceKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public interface ArtifactFactory {

    /**
     * Gets a new instance of an ItemStack based on the namespaced key provided.
     * This ItemStack will be a clone of the stack found in {@link Artifact}, with all its properties.
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @param amount The amount of the item.
     * @return the ItemStack.
     */
    static ItemStack getItem(NamespacedKey namespacedKey, int amount) {
        Artifact artifact = Artifacts.getArtifact(namespacedKey);
        return artifact.getItem(amount);
    }

    /**
     * Gets a new instance of an ItemStack based on the namespaced key provided.
     * This ItemStack will be a clone of the stack found in {@link Artifact}, with all its properties.
     * <br>
     * HOWEVER, this method will retrieve the ItemStack asynchronously, through a completable future.
     * If the artifact is not an instance of {@link AsyncArtifact}, the item will retrieved async through {@link #getItem(NamespacedKey, int)}.
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @param amount The amount of the item.
     * @return a CompletableFuture that will complete with the ItemStack of the artifact when it is retrieved.
     */
    static CompletableFuture<ItemStack> getItemAsync(NamespacedKey namespacedKey, int amount) {
        Artifact artifact = Artifacts.getArtifact(namespacedKey);
        if (artifact instanceof AsyncArtifact asyncArtifact) {
            return asyncArtifact.getItemAsync(amount);
        }
        return CompletableFuture.supplyAsync(() -> getItem(namespacedKey, amount));
    }

    /**
     * See {@link #getItem(NamespacedKey, int)}
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @return the ItemStack, with an amount of 1.
     */
    static ItemStack getItem(NamespacedKey namespacedKey) {
        return getItem(namespacedKey, 1);
    }

    /**
     * Registers an artifact with the given namespaced key. Optimally, the namespaced key should be
     * written inside {@link ArtifactNamespaceKeys}, to make fetching the artifact easier for other plugins.
     *
     * @param key The namespaced key of the artifact.
     * @param artifact The artifact to register.
     */
    static <T extends Artifact> void registerArtifact(NamespacedKey key, T artifact) {
        ArtifactRegistry.registerArtifact(key, artifact);
    }

}
