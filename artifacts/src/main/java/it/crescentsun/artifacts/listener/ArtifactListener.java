package it.crescentsun.artifacts.listener;

import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.artifacts.api.ArtifactUtil;
import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.ArtifactFlag;
import it.crescentsun.crescentcore.api.util.ItemUtils;
import it.crescentsun.crescentcore.api.util.PlayerUtils;
import it.crescentsun.crescentcore.api.util.SoundEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ArtifactListener implements Listener {

    private final Artifacts plugin;

    public ArtifactListener(Artifacts plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player interaction with custom artifact items.
     *
     * @param event The PlayerInteractEvent.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Artifact artifact = ArtifactUtil.identifyArtifact(item);
        if (artifact == null) {
            return;
        }

        plugin.getLogger().info("Artifact item interacted with by " + player.getName());

        event.setCancelled(true);
        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (isSneaking) {
                artifact.interactShiftRight(event);
            } else {
                artifact.interactRight(event);
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (isSneaking) {
                artifact.interactShiftLeft(event);
            } else {
                artifact.interactLeft(event);
            }
        }
    }

    /**
     * Handles clicks in the inventory involving custom artifact items.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }

        ItemStack slotItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        Artifact artifact = null;
        if (ItemUtils.isItemNull(slotItem) && !ItemUtils.isItemNull(cursorItem)) {
            // player has item on cursor
            artifact = ArtifactUtil.identifyArtifact(cursorItem);

            handleUnstackableClick(event, cursorItem, true);
        } else if (!ItemUtils.isItemNull(slotItem) && ItemUtils.isItemNull(cursorItem)) {
            // player has item in slot
            artifact = ArtifactUtil.identifyArtifact(slotItem);

            handleUnstackableClick(event, slotItem, false);
        }

        if (artifact == null) {
            return;
        }
        switch (event.getClick()) {
            case RIGHT -> artifact.clickRight(event);
            case LEFT -> artifact.clickLeft(event);
            case SHIFT_RIGHT -> artifact.clickShiftRight(event);
            case SHIFT_LEFT -> artifact.clickShiftLeft(event);
            case DROP -> artifact.clickSingleDrop(event);
            case MIDDLE -> artifact.clickMiddle(event);
            case NUMBER_KEY -> artifact.clickNumberKey(event);
            case DOUBLE_CLICK -> artifact.clickDouble(event);
            case WINDOW_BORDER_LEFT -> artifact.clickLeftWindowBorder(event);
            case WINDOW_BORDER_RIGHT -> artifact.clickRightWindowBorder(event);
            case CONTROL_DROP -> artifact.clickFullDrop(event);
            case SWAP_OFFHAND -> artifact.clickSwapOffHand(event);
            default -> {}
        }
    }

    /**
     * Handles drag events in the inventory involving custom artifact items.
     *
     * @param event The InventoryDragEvent.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }

        ItemStack item = event.getOldCursor();
        if (ArtifactUtil.hasFlag(item, ArtifactFlag.UNSPLITTABLE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles player drop item events with custom artifact items.
     *
     * @param event The PlayerDropItemEvent.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }
        ItemStack item = event.getItemDrop().getItemStack();
        if (ArtifactUtil.hasFlag(item, ArtifactFlag.UNSPLITTABLE)) {
            ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
            if (handItem.getAmount() > 1) {
                item.setAmount(item.getAmount() + handItem.getAmount());
                handItem.setAmount(0);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerAttemptPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        Artifact artifact = ArtifactUtil.identifyArtifact(item);
        if (artifact == null) {
            return;
        }
        // Check if players inventory is full
        if (event.getPlayer().getInventory().firstEmpty() == -1) {
            return;
        }
        if (!event.isCancelled()) {
            artifact.onPickup(event);
            if (event.isCancelled()) {
                return;
            }
            SoundEffect soundEffect = artifact.pickupSound();
            if (soundEffect == null) {
                return;
            }
            if (event.getFlyAtPlayer()) {
                soundEffect.playSoundAtLocation(event.getItem().getLocation());
            }
        }
    }

    /**
     * Handles unstackable artifact items.
     *
     * @param event The InventoryClickEvent.
     * @param item The item.
     * @param cursor Whether the item is on the cursor.
     */
    private void handleUnstackableClick(InventoryClickEvent event, ItemStack item, boolean cursor) {
        if (!ArtifactUtil.hasFlag(item, ArtifactFlag.UNSPLITTABLE)) return;
        if (event.getClick() == ClickType.RIGHT) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null) {
                PlayerUtils.spawnItemWithPlayerDropPhysics((Player) event.getWhoClicked(), item, EquipmentSlot.HAND);
                item.setAmount(0);
            } else {
                if (cursor) {
                    event.setCurrentItem(item);
                    event.getWhoClicked().setItemOnCursor(null);
                } else {
                    event.getWhoClicked().setItemOnCursor(item);
                    event.setCurrentItem(null);
                }
            }
        } else if (event.getClick() == ClickType.DROP) {
            event.setCancelled(true);
            PlayerUtils.spawnItemWithPlayerDropPhysics((Player) event.getWhoClicked(), item, EquipmentSlot.HAND);
            item.setAmount(0);
        }
    }
}
