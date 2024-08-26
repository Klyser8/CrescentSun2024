package it.crescentsun.artifacts.api;

import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.artifacts.item.Artifact;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static it.crescentsun.artifacts.registry.ArtifactRegistry.ARTIFACTS;

public class ArtifactUtil {

    /**
     * Gets the UUID of the given ItemStack.
     *
     * @param item
     * @return
     */
    @Nullable public static UUID getArtifactUniqueID(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        if (persistentDataContainer.get(Artifact.ITEM_INSTANCE_UUID, PersistentDataType.STRING) == null) {
            return null;
        }
        //noinspection ConstantConditions
        return UUID.fromString(persistentDataContainer.get(Artifact.ITEM_INSTANCE_UUID, PersistentDataType.STRING));
    }

    /**
     * Checks if the given ItemStack is an Artifact.
     * More specifically, it checks if the ItemStack has the ARTIFACT_KEY present in its PersistentDataContainer.
     *
     * @param item The ItemStack to check.
     * @return true if the ItemStack is an Artifact, false otherwise.
     */
    public static boolean isArtifact(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();
        return itemDataContainer.has(Artifact.ARTIFACT_KEY);
    }

    /**
     * Identifies an Artifact from an ItemStack by checking the PersistentDataContainer for the KlyItem's key.
     *
     * @param item The item to identify
     * @return The Artifact that the item is, or null if the item is not an Artifact
     */
    @Nullable  public static Artifact identifyArtifact(ItemStack item) {
        if (!isArtifact(item)) {
            return null;
        }
        Artifacts.getInstance().getLogger().config("Item '" + item + "' is an artifact");
        ItemMeta itemMeta = item.getItemMeta();
        String artifactKeyStr = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_KEY,
                PersistentDataType.STRING);
        if (artifactKeyStr == null) {
            Artifacts.getInstance().getLogger().info("Artifact key is null, unknown artifact");
            return null;
        }
        NamespacedKey artifactKey = NamespacedKey.fromString(artifactKeyStr);
        return ARTIFACTS.get(artifactKey);
    }
}
