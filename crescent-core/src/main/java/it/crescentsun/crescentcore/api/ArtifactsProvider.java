package it.crescentsun.crescentcore.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public interface ArtifactsProvider {

    /**
     * Gets a new instance of an ItemStack based on the namespaced key provided.
     * This ItemStack will be a clone of the stack in the artifact found at the namespaced key, with all its properties.
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @param amount The amount of the item.
     * @return the ItemStack.
     */
    ItemStack getItem(NamespacedKey namespacedKey, int amount);

    /**
     * Gets a new instance of an ItemStack based on the namespaced key provided.
     * This ItemStack will be a clone of the stack in the artifact found at the namespaced key, with all its properties.
     * <br>
     * HOWEVER, this method will retrieve the ItemStack asynchronously, through a completable future.
     * If the artifact is not an AsyncArtifact, the item will retrieved async through {@link #getItem(NamespacedKey, int)}.
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @param amount The amount of the item.
     * @return a CompletableFuture that will complete with the ItemStack of the artifact when it is retrieved.
     */
    CompletableFuture<ItemStack> getItemAsync(NamespacedKey namespacedKey, int amount);

    /**
     * See {@link #getItem(NamespacedKey, int)}
     *
     * @param namespacedKey The namespaced key of the artifact.
     * @return the ItemStack, with an amount of 1.
     */
    ItemStack getItem(NamespacedKey namespacedKey);

}
