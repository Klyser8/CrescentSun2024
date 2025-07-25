package it.crescentsun.artifacts.listener;

import it.crescentsun.api.artifacts.ArtifactProvider;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import it.crescentsun.api.crescentcore.util.ItemUtils;
import it.crescentsun.api.crescentcore.util.PlayerUtils;
import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.event.ArtifactInteractEvent;
import it.crescentsun.api.artifacts.event.ArtifactInventoryEvent;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.artifacts.event.ArtifactRegistrationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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

        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();
        ArtifactInteractEvent artifactInteractEvent = new ArtifactInteractEvent(artifact, item, player, action, event.getHand());
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (isSneaking) {
                event.setCancelled(artifact.interactShiftRight(artifactInteractEvent));
            } else {
                event.setCancelled(artifact.interactRight(artifactInteractEvent));
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (isSneaking) {
                event.setCancelled(artifact.interactShiftLeft(artifactInteractEvent));
            } else {
                event.setCancelled(artifact.interactLeft(artifactInteractEvent));
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

            if (ArtifactUtil.hasFlag(cursorItem, ArtifactFlag.UNSPLITTABLE)) {
                handleUnstackableClick(event, cursorItem, true);
            }
        } else if (!ItemUtils.isItemNull(slotItem) && ItemUtils.isItemNull(cursorItem)) {
            // player has item in slot
            artifact = ArtifactUtil.identifyArtifact(slotItem);
            if (ArtifactUtil.hasFlag(slotItem, ArtifactFlag.UNSPLITTABLE)) {
                handleUnstackableClick(event, slotItem, false);
            }
        }

        if (artifact == null) {
            return;
        }
        ArtifactInventoryEvent artifactInventoryEvent = new ArtifactInventoryEvent(event, ArtifactUtil.identifyArtifact(slotItem), ArtifactUtil.identifyArtifact(cursorItem));
        switch (event.getClick()) {
            case RIGHT -> event.setCancelled(artifact.clickRight(artifactInventoryEvent));
            case LEFT -> event.setCancelled(artifact.clickLeft(artifactInventoryEvent));
            case SHIFT_RIGHT -> event.setCancelled(artifact.clickShiftRight(artifactInventoryEvent));
            case SHIFT_LEFT -> event.setCancelled(artifact.clickShiftLeft(artifactInventoryEvent));
            case DOUBLE_CLICK -> event.setCancelled(artifact.clickDouble(artifactInventoryEvent));
            case MIDDLE-> //noinspection deprecation
                    event.setCancelled(artifact.clickMiddle(artifactInventoryEvent));
            case NUMBER_KEY -> event.setCancelled(artifact.clickNumberKey(artifactInventoryEvent));
            case SWAP_OFFHAND -> event.setCancelled(artifact.clickSwapOffHand(artifactInventoryEvent));
            case DROP -> event.setCancelled(artifact.dropSingle(artifactInventoryEvent));
            case CONTROL_DROP -> event.setCancelled(artifact.dropFull(artifactInventoryEvent));
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
                soundEffect.playAtLocation(event.getItem().getLocation());
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        Artifact artifact = ArtifactUtil.identifyArtifact(item);
        if (artifact == null) {
            return;
        }
        artifact.onItemSpawn(event);
    }

    /**
     * Handles unstackable artifact items.
     *
     * @param event The InventoryClickEvent.
     * @param item The item.
     * @param cursor Whether the item is on the cursor.
     */
    private void handleUnstackableClick(InventoryClickEvent event, ItemStack item, boolean cursor) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArtifactRegister(ArtifactRegistrationEvent event) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof ArtifactProvider artifactProvider) {
                artifactProvider.onArtifactRegister(event.getRegistryService());
            }
        }
    }

}
