package it.crescentsun.crystals.artifact;

import it.crescentsun.artifacts.item.ArtifactFlag;
import it.crescentsun.artifacts.item.AsyncArtifact;
import it.crescentsun.crescentmsg.CrescentColor;
import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crystals.Crystals;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.PlayerUtils;
import it.crescentsun.crescentcore.api.SoundEffect;
import it.crescentsun.crescentcore.api.event.crystals.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.core.data.ServerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CrystalArtifact extends AsyncArtifact {
    private final Crystals plugin;
    public static final NamespacedKey CRYSTAL_ID = new NamespacedKey("crystals", "crystal_id");

    public CrystalArtifact(Crystals plugin, NamespacedKey key, ItemStack defaultStack, String displayName, ArtifactFlag... flags) {
        super(key, defaultStack, displayName, flags);
        this.plugin = plugin;
    }

    /*@Override
    public ItemStack getItem(int amount) {
        ItemStack stack = super.getItem(amount);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(MessageFormatter.gradientText(
                Component.text(displayName), TextColor.color(254, 187, 255), TextColor.color(189, 92, 255))
                .decoration(TextDecoration.ITALIC, false));
        CompletableFuture<Integer> crystalsGenerated = KlyNetCore.getInstance().getDatabaseManager().getServerDataManager().getCrystalsGenerated();
        stack.setItemMeta(meta);
        crystalsGenerated.thenAcceptAsync(crystals -> {
            List<Component> lore = new ArrayList<>();
            lore.add(MessageFormatter.parse("<@dark_gray>ID: </@><@gray>" + crystals +"</@>"));
            Bukkit.getScheduler().runTask(plugin, () -> {
                System.out.println("Setting lore");
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.getPersistentDataContainer().set(CRYSTAL_ID, PersistentDataType.INTEGER, crystals + amount);
                itemMeta.lore(lore);
                stack.setItemMeta(itemMeta);
            });
        });
        return stack;
    }*/

    @Override
    public CompletableFuture<ItemStack> getItemAsync(int amount) {
        ServerDataManager serverDataManager = CrescentCore.getInstance().getDatabaseManager().getServerDataManager();
        return serverDataManager.getCrystalsGenerated().thenApply(crystalsGenerated -> {
            int id = crystalsGenerated + amount;
            BukkitScheduler scheduler = Bukkit.getScheduler();
            Executor mainExecutor = scheduler.getMainThreadExecutor(plugin);
            CompletableFuture<ItemStack> itemStackFuture = CompletableFuture.supplyAsync(() -> getItem(amount), mainExecutor);
            ItemStack itemStack = itemStackFuture.join();

            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(MessageFormatter.gradientText(
                            Component.text(displayName), TextColor.color(254, 187, 255), TextColor.color(189, 92, 255))
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(MessageFormatter.gradientText(Component.text("Release, absorb."), CrescentColor.PURPLE.getColor(), CrescentColor.BLUE.getColor()));
            lore.add(MessageFormatter.parse("<@dark_gray>ID: </@><@gray>" + id +"</@>"));
            meta.getPersistentDataContainer().set(CRYSTAL_ID, PersistentDataType.INTEGER, id);
            meta.lore(lore);
            itemStack.setItemMeta(meta);
            return itemStack;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public boolean rightClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return false;
        }
        Player player = event.getPlayer();
        if (event.getHand() == null) {
            return false;
        }
        PlayerUtils.spawnItemWithPlayerDropPhysics(player, item.clone(), event.getHand());
        item.setAmount(0);
        return true;
    }

    @Override
    public boolean onPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItem();
        if (!player.getUniqueId().equals(itemDrop.getThrower())) {
            if (itemDrop.getTicksLived() < plugin.getConfigProvider().getNonOwnedCrystalPickupDelay()) {
                event.setCancelled(true);
                return false;
            }
        }
        return true;
    }

}