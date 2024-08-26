package it.crescentsun.crescentcore.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    /**
     * Checks if an ItemStack is null or empty.
     *
     * @param item The ItemStack to check.
     * @return True if the ItemStack is null or empty, false otherwise.
     */
    public static boolean isItemNull(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

}
