package it.crescentsun.api.artifacts.item;

import it.crescentsun.api.artifacts.item.tooltip.*;
import it.crescentsun.api.common.PluginNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import it.crescentsun.api.artifacts.event.ArtifactInteractEvent;
import it.crescentsun.api.artifacts.event.ArtifactInventoryEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static it.crescentsun.api.artifacts.item.tooltip.Tooltip.createHeader;

/**
 * This abstract class represents a custom Artifact with its own click functionalities.
 * <br>
 * Each custom item should extend this class and implement the abstract methods to define their unique behavior.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public abstract class Artifact {

    // The NamespacedKey of the item's UUID. Used to identify unique items.
    public static final NamespacedKey ITEM_INSTANCE_UUID = new NamespacedKey(PluginNamespacedKeys.NAMESPACE_CRESCENTCORE, "uuid");
    // The NamespacedKey of the item's key. Used to identify the item.
    public static final NamespacedKey ARTIFACT_KEY = new NamespacedKey(PluginNamespacedKeys.NAMESPACE_CRESCENTCORE, "artifact_key");
    // The NamespacedKey of the item's Artifact Flags. Used set or fetch the item's flags.
    public static final NamespacedKey ARTIFACT_FLAGS = new NamespacedKey(PluginNamespacedKeys.NAMESPACE_CRESCENTCORE, "artifact_flags");
    public static final NamespacedKey CURRENT_TOOLTIP_PAGE = new NamespacedKey(PluginNamespacedKeys.NAMESPACE_CRESCENTCORE, "current_tooltip_page");
    protected static MiniMessage miniMessage = MiniMessage.builder().postProcessor(TooltipStyle::disableItalic).build();

    protected Tooltip tooltip;
    protected final TooltipStyle tooltipStyle;

    protected CrescentPlugin plugin;
    protected final Component displayName;         // The display name of the item
    protected final NamespacedKey namespacedKey;      // The NamespacedKey of the item
    protected final ItemStack defaultStack;     // The default ItemStack of the item. Used for cloning purposes.
    protected final ItemMeta defaultMeta;        // The custom ItemMeta of the item.
    protected final EnumSet<ArtifactFlag> defaultFlags;       // The default ArtifactFlags of the item.

    protected Artifact(CrescentPlugin plugin, NamespacedKey namespacedKey, ItemStack defaultStack, String displayNameText, TooltipStyle tooltipStyle, ArtifactFlag... defaultFlags) {
        this.plugin = plugin;
        this.namespacedKey = namespacedKey;
        this.displayName = miniMessage.deserialize(displayNameText);
        this.defaultStack = defaultStack;
        this.defaultMeta = defaultStack.getItemMeta().clone();
        this.tooltipStyle = tooltipStyle;
        createTooltip();
        defaultMeta.getPersistentDataContainer().set(ARTIFACT_KEY, PersistentDataType.STRING, namespacedKey.toString());
        if (tooltip != null && tooltip.getPages().size() > 1) {
            for (TooltipPage page : tooltip.getPages()) {
                if (!page.getSections().isEmpty()) {
                    TooltipSection lastSection = page.getSections().getLast();
                    lastSection.addContentLine(tooltipStyle.getTertiaryHex() + "          [shift right-click for more]");
                }
            }
        }

        if (defaultFlags == null || defaultFlags.length == 0) {
            this.defaultFlags = EnumSet.noneOf(ArtifactFlag.class);
            return;
        }

        this.defaultFlags = EnumSet.copyOf(Arrays.asList(defaultFlags));
        int[] flagIDs = this.defaultFlags.stream().mapToInt(ArtifactFlag::getId).toArray();
        defaultMeta.getPersistentDataContainer().set(ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY, flagIDs);
        // Last page of the tooltip should be the one displaying the flags
        List<String> flagLines = getFlagLines();
        if (flagLines.isEmpty()) {
            return;
        }
        tooltip =
            TooltipBuilder.builder(tooltip)
                .page()
                    .section(createHeader("FLAGS", tooltipStyle.getHeaderHex3()))
                        .addLines(flagLines)
                    .endSection()
                .endPage()
            .build();
    }

    /**
     * Override this method to create the tooltip for your artifact.
     * A tooltip can have up to three pages, with each page being up to 16 lines long and each line being up to 30 characters.
     * Surpassing any of these limits will result in a warning being logged. <br>
     *
     */
    protected abstract void createTooltip();

    /**
     * @return the tooltip of the artifact.
     */
    public Tooltip getTooltip() {
        return tooltip;
    }

    /**
     * @return the display name of the artifact's default stack.
     */
    public Component getDisplayName() {
        return defaultMeta.displayName();
    }

    /**
     * Creates a new ItemStack of this Artifact with the specified amount.
     *
     * @param amount The desired amount of items in the ItemStack, capped at 64.
     * @return An ItemStack of this Artifact with the specified amount.
     */
    public ItemStack createStack(int amount) {
        int maxStackSize = defaultMeta.getMaxStackSize();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        ItemStack clone = defaultStack.clone();
        ItemMeta clonedMeta = defaultMeta.clone();

        // Ensure the core artifact data is always present on the created stack
        PersistentDataContainer data = clonedMeta.getPersistentDataContainer();
        data.set(ARTIFACT_KEY, PersistentDataType.STRING, namespacedKey.toString());
        if (!defaultFlags.isEmpty()) {
            int[] flagIDs = this.defaultFlags.stream().mapToInt(ArtifactFlag::getId).toArray();
            data.set(ARTIFACT_FLAGS, PersistentDataType.INTEGER_ARRAY, flagIDs);
        }


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
        clonedMeta.displayName(displayName);
        if (tooltip != null && !tooltip.getPages().isEmpty()) {
            TooltipPage firstPage = tooltip.getPages().getFirst();
            clonedMeta.lore(firstPage.assembleLore(miniMessage, tooltipStyle));
            clonedMeta.getPersistentDataContainer().set(CURRENT_TOOLTIP_PAGE, PersistentDataType.INTEGER, 0);
        }
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
        ItemStack item = createStack(amount);
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

    /**
     * Called when a player middle-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise.
     *
     * @deprecated currently buggy, see <a href="https://github.com/PaperMC/Paper/issues/11465">Paper Issue #11465</a>
     */
    @Deprecated
    public boolean clickMiddle(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player right-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.player.PlayerInteractEvent event} will be cancelled.
     */
    public boolean interactRight(ArtifactInteractEvent event) {
        return false;
    }

    /**
     * Called when a player left-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.player.PlayerInteractEvent event} will be cancelled.
     */
    public boolean interactLeft(ArtifactInteractEvent event) {
        return false;
    }

    /**
     * Called when a player shift-right-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.player.PlayerInteractEvent event} will be cancelled.
     */
    public boolean interactShiftRight(ArtifactInteractEvent event) {
        return false;
    }

    /**
     * Called when a player shift-left-clicks with this Artifact.
     *
     * @param event The PlayerInteractEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.player.PlayerInteractEvent event} will be cancelled.
     */
    public boolean interactShiftLeft(ArtifactInteractEvent event) {
        return false;
    }

    /**
     * Called when a player right-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickRight(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player left-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickLeft(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player shift-right-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickShiftRight(ArtifactInventoryEvent event) {
        Artifact artifact = event.getClickedArtifact();
        ItemStack item = event.getOriginalEvent().getCurrentItem();
        if (item == null || artifact == null) {
            return false;
        }
        if (artifact.getTooltip() != null && artifact.getTooltip().getPages().size() > 1) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            int pageIndex = data.getOrDefault(CURRENT_TOOLTIP_PAGE, PersistentDataType.INTEGER, 0);
            pageIndex = (pageIndex + 1) % artifact.getTooltip().getPages().size();
            TooltipPage page = artifact.getTooltip().getPages().get(pageIndex);
            meta.lore(page.assembleLore(miniMessage, tooltipStyle));
            data.set(CURRENT_TOOLTIP_PAGE, PersistentDataType.INTEGER, pageIndex);
            item.setItemMeta(meta);
            return true;
        }
        return false;
    }

    /**
     * Called when a player shift-left-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickShiftLeft(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player uses a number key with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickNumberKey(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player double-clicks with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickDouble(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player swaps offhand with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean clickSwapOffHand(ArtifactInventoryEvent event) {
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

    /**
     * Called when a player drops an instance of this artifact from the inventory, with the Q button.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean dropSingle(ArtifactInventoryEvent event) {
        return false;
    }

    /**
     * Called when a player uses control drop with this Artifact in an inventory.
     *
     * @param event The InventoryClickEvent that triggered this action.
     * @return true if the interaction was successful, false otherwise. If true,
     * the original {@link org.bukkit.event.inventory.InventoryClickEvent event} will be cancelled.
     */
    public boolean dropFull(ArtifactInventoryEvent event) {
        return false;
    }

    public boolean onPickup(PlayerAttemptPickupItemEvent event) {
        return false;
    }

    public boolean onItemSpawn(ItemSpawnEvent event) {
        return false;
    }

    private List<String> getFlagLines() {
        List<String> lines = new ArrayList<>();
        for (ArtifactFlag flag : this.defaultFlags) {
            if (!flag.getDescription().isEmpty()) {
                lines.add(tooltipStyle.getPrimaryHex3() + flag.getName());
                lines.add(tooltipStyle.getSecondaryHex3() + " " + flag.getDescription());
            }
        }
        return lines;
    }

}
