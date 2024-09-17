package it.crescentsun.artifacts.api;

import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.ArtifactFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
     * Checks if the given ItemStack has the given ArtifactFlag.
     * This method checks the ItemStack's PersistentDataContainer for the ARTIFACT_FLAGS key, and then controls
     * each flag's id found in the array. If a matching id is found, the method returns true.
     *
     * @param item The ItemStack to check.
     * @param artifactFlag The ArtifactFlag to check for.
     * @return true if the ItemStack has the given flag, false otherwise.
     */
    public static boolean hasFlag(@NotNull ItemStack item, @NotNull ArtifactFlag artifactFlag) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        int[] flags = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY);
        if (flags == null) {
            return false;
        }
        for (int flag : flags) {
            if (flag == artifactFlag.getId()) {
                return true;
            }
        }
        return false;
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

    /**
     * Removes the specified ArtifactFlags from the given ItemStack.
     *
     * @param item The ItemStack from which to remove the flags.
     * @param toBeRemoved The ArtifactFlags to be removed from the ItemStack.
     * @return The modified ItemStack with the specified flags removed.
     */
    public static ItemStack removeFlagsFromStack(ItemStack item, ArtifactFlag... toBeRemoved) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return item;
        }
        int[] oldFlags = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY);
        if (oldFlags == null) {
            return item;
        }

        List<Integer> flagsList = new ArrayList<>();
        for (int flagId : oldFlags) {
            flagsList.add(flagId);
        }

        for (ArtifactFlag flag : toBeRemoved) {
            int flagId = flag.getId();
            flagsList.removeIf(existingFlag -> existingFlag == flagId);
        }

        int[] newFlags = flagsList.stream().mapToInt(Integer::intValue).toArray();

        itemMeta.getPersistentDataContainer().set(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY, newFlags);
        item.setItemMeta(itemMeta);
        return item;
    }

    /**
     * Adds the specified ArtifactFlags to the given ItemStack.
     * This method updates the ItemStack's PersistentDataContainer with the new flags, avoiding duplicates.
     *
     * @param item The ItemStack to which the flags will be added.
     * @param toAdd The ArtifactFlags to be added to the ItemStack.
     * @return The modified ItemStack with the specified flags added.
     */
    public static ItemStack addFlagsToStack(ItemStack item, ArtifactFlag... toAdd) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return item;
        }

        // Retrieve existing flags from the persistent data container
        int[] oldFlags = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY);
        List<Integer> flagsList = getNewFlags(toAdd, oldFlags);

        // Convert the list back to an array
        int[] newFlags = flagsList.stream().mapToInt(Integer::intValue).toArray();

        // Update the persistent data container with the new flags
        itemMeta.getPersistentDataContainer().set(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY, newFlags);
        item.setItemMeta(itemMeta);

        return item;
    }

    private static @NotNull List<Integer> getNewFlags(ArtifactFlag[] toAdd, int[] oldFlags) {
        List<Integer> flagsList;

        if (oldFlags != null) {
            // Convert the array to a list for easier manipulation
            flagsList = new ArrayList<>();
            for (int flagId : oldFlags) {
                flagsList.add(flagId);
            }
        } else {
            // If there are no existing flags, initialize a new list
            flagsList = new ArrayList<>();
        }

        // Add new flags, avoiding duplicates
        for (ArtifactFlag flag : toAdd) {
            int flagId = flag.getId();
            if (!flagsList.contains(flagId)) {
                flagsList.add(flagId);
            }
        }
        return flagsList;
    }


}
