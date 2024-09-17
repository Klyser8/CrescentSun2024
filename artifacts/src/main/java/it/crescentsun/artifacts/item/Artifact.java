package it.crescentsun.artifacts.item;

import it.crescentsun.crescentcore.api.registry.CrescentNamespacedKeys;
import it.crescentsun.crescentcore.api.util.SoundEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

/**
 * This abstract class represents a custom Artifact with its own click functionalities.
 * <br>
 * Each custom item should extend this class and implement the abstract methods to define their unique behavior.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public abstract class Artifact {

    // The NamespacedKey of the item's UUID. Used to identify unique items.
    public static final NamespacedKey ITEM_INSTANCE_UUID = new NamespacedKey(CrescentNamespacedKeys.NAMESPACE_ARTIFACTS, "uuid");
    // The NamespacedKey of the item's key. Used to identify the item.
    public static final NamespacedKey ARTIFACT_KEY = new NamespacedKey(CrescentNamespacedKeys.NAMESPACE_ARTIFACTS, "artifact_key");
    // The NamespacedKey of the item's Artifact Flags. Used set or fetch the item's flags.
    public static final NamespacedKey ARTIFACT_FLAGS = new NamespacedKey(CrescentNamespacedKeys.NAMESPACE_ARTIFACTS, "artifact_flags");
    private static int customModelDataCounter = 1;

    protected final String displayName;         // The display name of the item
    protected final NamespacedKey namespacedKey;      // The NamespacedKey of the item
    protected final ItemStack defaultStack;     // The default ItemStack of the item. Used for cloning purposes.
    protected final ItemMeta customMeta;        // The custom ItemMeta of the item.
    protected final int customModelData;        // The custom model data of the item. Used for custom textures.
    protected final EnumSet<ArtifactFlag> defaultFlags;       // The default ArtifactFlags of the item.

    protected Artifact(NamespacedKey namespacedKey, ItemStack defaultStack, String displayName, ArtifactFlag... defaultFlags) {
        this.namespacedKey = namespacedKey;
        this.displayName = displayName;
        this.defaultStack = defaultStack;
        this.customModelData = customModelDataCounter++;
        this.customMeta = defaultStack.getItemMeta();
        if (defaultFlags == null || defaultFlags.length == 0) {
            this.defaultFlags = EnumSet.noneOf(ArtifactFlag.class);
        }
        else {
            this.defaultFlags = EnumSet.copyOf(Arrays.asList(defaultFlags));
        }
        int[] flagIDs = this.defaultFlags.stream().mapToInt(ArtifactFlag::getId).toArray();
        //If item is unique, add a UUID to the item's PersistentDataContainer.
        customMeta.getPersistentDataContainer().set(ARTIFACT_KEY, PersistentDataType.STRING,  namespacedKey.toString());
        customMeta.getPersistentDataContainer().set(ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY, flagIDs);
        customMeta.setCustomModelData(customModelData);
        defaultStack.setItemMeta(customMeta);
    }

    /**
     * Called when a player right-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean interactRight(PlayerInteractEvent event) {
        return false;
    }

    /**
     * Called when a player left-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean interactLeft(PlayerInteractEvent event) {
        return false;
    }

    /**
     * Called when a player shift-right-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean interactShiftRight(PlayerInteractEvent event) {
        return false;
    }

    /**
     * Called when a player shift-left-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean interactShiftLeft(PlayerInteractEvent event) {
        return false;
    }

    /**
     * Called when a player right-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickRight(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player left-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickLeft(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player shift-right-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickShiftRight(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player shift-left-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickShiftLeft(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player drops an instance of this artifact from the inventory, with the Q button.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickSingleDrop(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player middle-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickMiddle(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player uses a number key with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickNumberKey(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player double-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickDouble(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player clicks the left window border with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickLeftWindowBorder(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player clicks the right window border with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickRightWindowBorder(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player uses control drop with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickFullDrop(InventoryClickEvent event) {
        return false;
    }

    /**
     * Called when a player swaps offhand with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     */
    public boolean clickSwapOffHand(InventoryClickEvent event) {
        return false;
    }

    /**
     * The sound that plays when the Artifact is picked up.
     * Null by default.
     * @return The sound that plays when the Artifact is picked up.
     */
    @Nullable public SoundEffect pickupSound() {
        return null;
    }

    public boolean onPickup(PlayerAttemptPickupItemEvent event) {
        return false;
    }

    /**
     * Creates a new ItemStack of this Artifact with the specified amount.
     *
     * @param amount The desired amount of items in the ItemStack, capped at 64.
     * @return An ItemStack of this Artifact with the specified amount.
     */
    public ItemStack getItem(int amount) {
        int maxStackSize = defaultStack.getMaxStackSize();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        ItemStack clone = defaultStack.clone();
        ItemMeta clonedMeta = clone.getItemMeta();
        if (hasFlag(ArtifactFlag.UNIQUE)) {
            clonedMeta.getPersistentDataContainer().set(ITEM_INSTANCE_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());
        }
        if (hasFlag(ArtifactFlag.WITH_GLINT)) {
            boolean hadPriorEnchants = clonedMeta.hasEnchants();
            if (!hadPriorEnchants) {
                clonedMeta.addEnchant(Enchantment.INFINITY, 1, false);
                clonedMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }
        clonedMeta.displayName(Component.text(displayName)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(255, 255, 255)));
        clone.setItemMeta(clonedMeta);
        clone.setAmount(amount);
        return clone;
    }

    /**
     * Spawns an item of this Artifact at the specified location with the specified amount.
     *
     * @param location The location to spawn the artifact drop at.
     * @param amount The stack size of the artifact drop.
     * @return The Item entity that was spawned.
     */
    public Item spawnItem(Location location, int amount) {
        ItemStack item = getItem(amount);
        return location.getWorld().dropItem(location, item);
    }

    public NamespacedKey namespacedKey() {
        return namespacedKey;
    }

    /**
     * Checks if this Artifact has an ArtifactFlag by default.
     * @param flag The ArtifactFlag to check for.
     *
     * @return true if the item has the specified ArtifactFlag, false otherwise.
     */
    public boolean hasFlag(ArtifactFlag flag) {
        return defaultFlags.contains(flag);
    }

}
