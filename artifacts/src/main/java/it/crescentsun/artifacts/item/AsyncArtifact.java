package it.crescentsun.artifacts.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

/**
 * An artifact that can be retrieved asynchronously.
 * Make your custom item extend this class if you want to retrieve it asynchronously.
 * If your custom item requires a database call or any other long-running operation to be retrieved, it should extend this class.
 */
public abstract class AsyncArtifact extends Artifact {
    protected AsyncArtifact(NamespacedKey itemKey, ItemStack defaultStack, String displayName, ArtifactFlag... flags) {
        super(itemKey, defaultStack, displayName, flags);
    }

    /**
     * Retrieves the ItemStack of the artifact asynchronously.
     *
     * @param amount The amount of the item to retrieve
     * @return A CompletableFuture that will complete with the ItemStack of the artifact when it is retrieved.
     */
    public CompletableFuture<ItemStack> getItemAsync(int amount) {
        return CompletableFuture.supplyAsync(() -> getItem(amount));
    }

    @Override
    @Deprecated
    public ItemStack getItem(int amount) {
        return super.getItem(amount);
    }
}
