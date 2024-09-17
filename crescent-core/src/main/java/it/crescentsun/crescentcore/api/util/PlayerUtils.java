package it.crescentsun.crescentcore.api.util;

import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerUtils {

    /**
     * Gets the PlayerData for a player, using the Player object.
     *
     * @param player The player to get the PlayerData for.
     * @return The PlayerData for the player.
     *
     * @deprecated See {@link #getPlayerData(UUID)}
     */
    @Deprecated
    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Gets the PlayerData for a player, using the UUID of the player.
     *
     * @param uuid The UUID of the player.
     * @return The PlayerData for the player.
     *
     * @deprecated To be moved to {@link CrescentCore}
     */
    @Deprecated
    public static PlayerData getPlayerData(UUID uuid) {
        return CrescentCore.getInstance().getPlayerDataManager().getData(uuid);
    }

    /**
     * Drops an ItemStack with player drop physics.
     *
     * @param player The player to drop the item for.
     * @param item The ItemStack to drop.
     * @param hand The hand to swing when dropping the item. Can be null to not swing a hand.
     *
     * @return The dropped Item.
     */
    public static Item spawnItemWithPlayerDropPhysics(Player player, ItemStack item, @Nullable EquipmentSlot hand) {
        if (item == null) {
            return null;
        }
        Location playerLocation = player.getLocation().add(0, 1.3, 0);
        Vector playerDirection = playerLocation.getDirection().normalize();
        Location adjustedLoc = playerLocation.add(playerDirection.multiply(0.5));
        Item droppedItem = player.getWorld().dropItem(adjustedLoc, item);
        PlayerDropItemEvent dropEvent = new PlayerDropItemEvent(player, droppedItem);
        if (dropEvent.isCancelled()) {
            droppedItem.remove();
            return null;
        }
        dropEvent.callEvent();
        droppedItem.setPickupDelay(30);
        droppedItem.setVelocity(playerDirection.multiply(0.66));
        droppedItem.setThrower(player.getUniqueId());
        if (hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND) {
            player.swingHand(hand);
        }
        return droppedItem;
    }
    /**
     * Checks if a player is on the ground.
     * As the paper provided method {@link Player#isOnGround()} isn't reliable due to spoofing risks, this method
     * uses raytracing to check if the player's got a block 1/16th of a block underneath them.
     *
     * @param player The player to check.
     * @return Whether the player is on the ground or not.
     */
    public static boolean isOnGround(Player player) {
        // Using raytracing, check the first block hit underneath.
        World world = player.getWorld();
        RayTraceResult rayTraceResult = world.rayTraceBlocks(
                player.getLocation(),
                new Vector(0, -1, 0),
                1.0,
                FluidCollisionMode.NEVER,
                true
        );
        boolean isOnGround = rayTraceResult != null && rayTraceResult.getHitBlock() != null;
        return isOnGround;
    }

}
