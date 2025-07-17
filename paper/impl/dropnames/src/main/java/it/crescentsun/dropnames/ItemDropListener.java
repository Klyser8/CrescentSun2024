package it.crescentsun.dropnames;

import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.event.ArtifactFlagChangeEvent;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

public class ItemDropListener implements Listener {

    private final DropNames plugin;
    public ItemDropListener(DropNames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDropSpawn(ItemSpawnEvent event) {
        Item itemDrop = event.getEntity();
        updateDropName(itemDrop);
    }

    @EventHandler
    public void onItemDropMerge(ItemMergeEvent event) {
        Item itemDrop = event.getTarget();
        for (Entity passenger : itemDrop.getPassengers()) {
            if (passenger instanceof TextDisplay) {
                passenger.remove();
            }
        }
        for (Entity passenger : event.getEntity().getPassengers()) {
            if (passenger instanceof TextDisplay) {
                passenger.remove();
            }
        }
        // Get sum of amounts of both items
        int amount = event.getTarget().getItemStack().getAmount() + event.getEntity().getItemStack().getAmount();
        updateDropName(itemDrop, amount);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof TextDisplay textDisplay && event.getDismounted() instanceof Item) {
            textDisplay.remove();
        }
    }

    @EventHandler
    public void onArtifactFlagRemove(ArtifactFlagChangeEvent event) {
        if (event.getRemovedFlags().isEmpty()) {
            return;
        }
        Item itemEntity = event.getItemEntity();
        if (itemEntity == null) {
            return;
        }
        if (event.getRemovedFlags().contains(ArtifactFlag.HIDE_DROP_NAME)) {
            updateDropName(itemEntity);
        }

    }

    private void updateDropName(Item itemDrop) {
        updateDropName(itemDrop, 0);
    }

    private void updateDropName(Item itemDrop, int amountOverride) {
        ItemStack stack = itemDrop.getItemStack();
        if (ArtifactUtil.hasFlag(stack, ArtifactFlag.HIDE_DROP_NAME)) {
            return;
        }
        World world = itemDrop.getWorld();
        Component itemName;
        if (stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()) {
            itemName = stack.getItemMeta().displayName();
        } else {
            itemName = stack.displayName();
        }
        if (itemName == null) {
            return;
        }
        TextDisplay dropName = world.spawn(itemDrop.getLocation(), TextDisplay.class);
        dropName.setBillboard(Display.Billboard.CENTER);
        itemDrop.addPassenger(dropName);
        dropName.setAlignment(TextDisplay.TextAlignment.CENTER);
        int amount;
        if (amountOverride == 0) {
            amount = stack.getAmount();
        } else {
            amount = amountOverride;
        }
        String dropFormat = plugin.getDropNamesConfig().getDropFormat()
                .replace("%amount%", String.valueOf(amount));
        dropFormat = dropFormat.replace("%name%", MiniMessage.miniMessage().serialize(itemName));
        dropName.text(MiniMessage.miniMessage().deserialize(dropFormat));
        dropName.setShadowed(false);
        dropName.setViewRange(plugin.getDropNamesConfig().getMaxRenderDistance());
        dropName.setSeeThrough(false);
        Transformation transform = dropName.getTransformation();
        transform.getTranslation().add(0.0f, 0.25f, 0.0f);
        dropName.setTransformation(transform);
    }

}
