package it.crescentsun.crystals;

import io.papermc.paper.advancement.AdvancementDisplay;
import it.crescentsun.api.artifacts.ArtifactUtil;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.api.crystals.CrystalSource;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.api.crystals.event.AddCrystalsEvent;
import it.crescentsun.api.crystals.event.RemoveCrystalsEvent;
import it.crescentsun.api.crystals.event.SpawnCrystalsEvent;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrystalListener implements Listener {

    private final Crystals plugin;

    public CrystalListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        AdvancementDisplay display = event.getAdvancement().getDisplay();
        if (display == null) {
            return;
        }
        AdvancementDisplay.Frame frame = display.frame();
        if (frame.equals(AdvancementDisplay.Frame.CHALLENGE)) {
            plugin.getCrystalsService().spawnCrystals(player, 4, CrystalSource.ADVANCEMENT,
                    CrystalSpawnAnimation.CIRCLING_EXPLOSION, player.getLocation().add(0, 1, 0));
        } else {
            plugin.getCrystalsService().spawnCrystals(player, 1, CrystalSource.ADVANCEMENT,
                    CrystalSpawnAnimation.HOVER, player.getLocation().add(0, 1, 0));
        }

        // If the player gets any advancement, we assume they have claimed their crystals. TODO: TEST!
        PlayerData playerData = plugin.getPlayerDataService().getData(player);
        Optional<Boolean> hasPlayerClaimedCrystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_CLAIMED);
        if (hasPlayerClaimedCrystals.isPresent() && hasPlayerClaimedCrystals.get()) {
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_CLAIMED, true);
        }
    }

    @EventHandler
    public void onPlayerJoinPostDataLoad(PlayerJoinEventPostDBLoad event) {
        // Loop over all the advancements the player has completed
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataService().getData(player);
        if (!plugin.getCrescentCoreAPI().getServerName().contains("crescentcraft")) {
            return;
        }
        Optional<Boolean> dataValue = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_CLAIMED);
        if (dataValue.isPresent() && dataValue.get()) {
            return;
        }
        Map<Advancement, Integer> completedAdvancements = new HashMap<>();
        Bukkit.advancementIterator().forEachRemaining(advancement -> {
            if (player.getAdvancementProgress(advancement).isDone()) {
                AdvancementDisplay display = advancement.getDisplay();
                if (display != null) {
                    boolean isChallenge = display.frame().equals(AdvancementDisplay.Frame.CHALLENGE);
                    completedAdvancements.put(advancement, isChallenge ? 4 : 1);
                }
            }
        });
        int crystalsToSpawn = completedAdvancements.values().stream().mapToInt(Integer::intValue).sum();
        if (crystalsToSpawn > 0) {
            plugin.getCrystalsService().spawnCrystals(player, crystalsToSpawn, CrystalSource.ADVANCEMENT,
                    CrystalSpawnAnimation.CIRCLING_EXPLOSION, player.getLocation().add(0, 1, 0));
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_CLAIMED, true);
        }
    }

    @EventHandler
    public void onCrystalSpawn(SpawnCrystalsEvent event) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Player player = event.getOwner();
        if (player == null) {
            return;
        }
        int amount = event.getAmount();
        PlayerData playerData = plugin.getPlayerDataService().getData(player);
        Optional<Integer> currentCrystals = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_SPAWNED);
        playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_SPAWNED, amount + currentCrystals.orElse(0));
        if (event.getOwner() != null) {
        player.sendMessage(miniMessage.deserialize(
                CrescentHexCodes.ICE_CITADEL + amount + CrescentHexCodes.DROPLET + " crystal(s) have appeared before you. Keep them safe!" ));
        }
    }

    @EventHandler
    public void onCrystalAdd(AddCrystalsEvent event) {
        if (event.getSource() == CrystalSource.COMMAND || event.getSource() == CrystalSource.SALE) {
            plugin.getStatistics().setCrystalsGenerated(plugin.getStatistics().getCrystalsGenerated() + event.getAddedAmount());
        }
    }

    @EventHandler
    public void onCrystalRemove(RemoveCrystalsEvent event) {
        if (event.getSource() == CrystalSource.SALE) {
            plugin.getStatistics().setCrystalsSpent(plugin.getStatistics().getCrystalsSpent() + event.getRemovedAmount());
        } else {
            plugin.getStatistics().setCrystalsLost(plugin.getStatistics().getCrystalsLost() + event.getRemovedAmount());
        }
    }

    @EventHandler
    public void onCrystalPickup(PlayerAttemptPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        Artifact artifact = ArtifactUtil.identifyArtifact(item);
        if (artifact == null) {
            return;
        }
        if (artifact.namespacedKey().equals(ArtifactNamespacedKeys.CRYSTAL)) {
            plugin.getCrystalsSFX().crystalPickUp.playForPlayerAtLocation(event.getPlayer());
        }
    }

}
