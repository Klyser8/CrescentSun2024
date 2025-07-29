package it.crescentsun.crystals.vault;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.crystals.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class VaultPreventNonCrystalListener implements Listener {

    private final Crystals plugin;

    public VaultPreventNonCrystalListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!isVaultView(e.getView())) return;
        Inventory top = e.getView().getTopInventory();
        Inventory clicked = e.getClickedInventory();
        if (clicked == null) return;

        // Whether the player clicked in the vault inventory
        boolean inVault = (e.getRawSlot() < top.getSize());

        InventoryAction action = e.getAction();
        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // 1) Check for shift click
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (!inVault) {
                if (!isCrystal(current)) {
                    e.setCancelled(true);
                }
            }
            return;
        }

        // 2) Other click based placements and removals
        if (inVault) {
            switch (action) {
                // placing from cursor into vault
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                    if (!isCrystal(cursor)) {
                        e.setCancelled(true);
                    }
                    break;

                // hotbar keyswap into vault
                case HOTBAR_SWAP:
                    ItemStack hot = e.getWhoClicked()
                            .getInventory()
                            .getItem(e.getHotbarButton());
                    if (!isCrystal(hot)) {
                        e.setCancelled(true);
                    }
                    break;

                // picking up from vault is allowed
                case PICKUP_ALL:
                case PICKUP_HALF://
                case PICKUP_ONE:
                case PICKUP_SOME:
                    // allowed: user can always withdraw crystla artifacts
                    break;

                default:
                    // Ignore other actions
                    break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!isVaultView(e.getView())) return;
        int topSize = e.getView().getTopInventory().getSize();

        // any rawSlot < topSize is in the vault
        for (int raw : e.getRawSlots()) {
            if (raw < topSize) {
                ItemStack attempted = e.getNewItems().get(raw);
                if (!isCrystal(attempted)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent e) {
        if (!isVaultView(e.getView())) return;
        if (!isCrystal(e.getCursor())) {
            e.setCancelled(true);
        }
    }

    private boolean isCrystal(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        var artifact = ArtifactUtil.identifyArtifact(stack);
        return artifact != null &&
                artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL);
    }

    private boolean isVaultView(InventoryView view) {
        return view.getTopInventory().getHolder() instanceof VaultInventory;
    }
}

