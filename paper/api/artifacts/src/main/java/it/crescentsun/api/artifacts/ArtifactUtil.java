package it.crescentsun.api.artifacts;

import it.crescentsun.api.artifacts.event.ArtifactFlagChangeEvent;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        ArtifactRegistryService registryService = getArtifactRegistryService();
        if (registryService == null) {
            throw new IllegalStateException("ArtifactRegistryService is not available. Is the plugin enabled?");
        }
        ItemMeta itemMeta = item.getItemMeta();
        String artifactKeyStr = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_KEY,
                PersistentDataType.STRING);
        if (artifactKeyStr == null) {
            return null;
        }
        NamespacedKey artifactKey = NamespacedKey.fromString(artifactKeyStr);
        return registryService.getArtifact(artifactKey);
    }

    private static ArtifactRegistryService getArtifactRegistryService() {
        ServicesManager servicesManager = Bukkit.getServicesManager();
        RegisteredServiceProvider<ArtifactRegistryService> rsp = servicesManager.getRegistration(ArtifactRegistryService.class);
        if (rsp != null) {
            return rsp.getProvider();
        }
        return null;
    }

    /**
     * Removes the specified ArtifactFlags from the given ItemStack.
     *
     * @param item The ItemStack from which to remove the flags.
     * @param toBeRemoved The ArtifactFlags to be removed from the ItemStack.
     */
    public static void removeFlagsFromItem(ItemStack item, ArtifactFlag... toBeRemoved) {
        removeFlags(item, null, toBeRemoved);
    }

    public static void removeFlagsFromItem(Item itemEntity, ArtifactFlag... toBeRemoved) {
        removeFlags(itemEntity.getItemStack(), itemEntity, toBeRemoved);
    }

    private static void removeFlags(ItemStack stack, @Nullable Item itemEntity, ArtifactFlag... toBeRemoved) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        int[] oldFlags = itemMeta.getPersistentDataContainer().get(Artifact.ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY);
        if (oldFlags == null) {
            return;
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
        stack.setItemMeta(itemMeta);
        if (itemEntity != null) {
            itemEntity.setItemStack(stack);
        }
        ArtifactFlagChangeEvent event = new ArtifactFlagChangeEvent(identifyArtifact(stack), stack, itemEntity, null, List.of(toBeRemoved));
        event.callEvent();
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

        // If any of the flags being added is "UNIQUE", a unique UUID must be applied to the stack.
        if (toAdd != null) {
            for (ArtifactFlag flag : toAdd) {
                if (flag == ArtifactFlag.UNIQUE) {
                    UUID uniqueId = UUID.randomUUID();
                    itemMeta.getPersistentDataContainer().set(Artifact.ITEM_INSTANCE_UUID, PersistentDataType.STRING, uniqueId.toString());
                    break; // Only need to set it once
                }
            }
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
