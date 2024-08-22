package it.klynet.klynetcore.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    /**
     * Checks if a player's inventory is full.
     *
     * @param player The player to check.
     * @return True if the player's inventory is full, false otherwise.
     */
    public static boolean isInventoryFull(Player player) {
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                // found an empty slot, inventory is not full
                return false;
            }
        }
        // inventory is full
        return true;
    }

    /**
     * Checks if a player's inventory is full, or if a given ItemStack would fit into the inventory.
     *
     * @param player the player to check
     * @param itemStack the ItemStack to check for fitting into the inventory
     * @return true if the inventory is full or the ItemStack doesn't fit, false otherwise
     */
    public static boolean isInventoryFull(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        int emptySlots = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                // found an empty slot
                emptySlots++;
                if (emptySlots > 1) {
                    // inventory is not full, and the item would fit
                    return false;
                }
            } else if (item.isSimilar(itemStack) && item.getAmount() < item.getMaxStackSize()) {
                // found a stackable item with room for more
                // calculate how many more of this item could fit
                int remaining = item.getMaxStackSize() - item.getAmount();
                if (remaining >= itemStack.getAmount()) {
                    // the entire ItemStack would fit
                    return false;
                } else {
                    // only part of the ItemStack would fit, keep searching
                    emptySlots++;
                    if (emptySlots > 1) {
                        // inventory is not full, and the item would fit
                        return false;
                    }
                    itemStack.setAmount(itemStack.getAmount() - remaining);
                }
            }
        }
        // inventory is full, or the item doesn't fit
        return true;
    }


}
