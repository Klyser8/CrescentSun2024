package it.klynet.crystals.crystalix.listener;

import it.klynet.crystals.Crystals;
import it.klynet.klynetcore.api.PlayerUtils;
import it.klynet.klynetcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.klynet.klynetcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import it.klynet.klynetcore.core.data.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final Crystals plugin;

    public PlayerListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDataLoadedPostJoin(PlayerJoinEventPostDBLoad event) {
        Player player = event.getPlayer();
        PlayerData playerData = event.getPlayerData();
        if (playerData == null) {
            return;
        }
        if (playerData.getData(KlyNetNamespaceKeys.SETTINGS_SHOW_CRYSTALIX)) {
            plugin.getCrystalixManager().createCrystalix(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCrystalixManager().getCrystalix(player) == null) {
            return;
        }
        Bukkit.getLogger().info("Removing crystalix for " + player.getName());
        plugin.getCrystalixManager().removeCrystalix(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerLoadPostDB(ServerLoadPostDBSetupEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = PlayerUtils.getPlayerData(player);
            if (data.getData(KlyNetNamespaceKeys.SETTINGS_SHOW_CRYSTALIX)) {
                plugin.getCrystalixManager().createCrystalix(player);
            }
        }
    }

    /**
     * Used to allow players to summon the Crystalix with a custom item.
     */
/*    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }
        CrystalixEntity crystalix = plugin.getCrystalixManager().getCrystalix(player);
        Entity rightClicked = event.getRightClicked();
        if (rightClicked.equals(crystalix.getItem())) {
//            player.getInventory().setItemInMainHand(
                    Artifacts.getArtifact(ArtifactRegistry.KLYSTARIX_KEY).build(1));
            plugin.getCrystalixManager().removeCrystalix(player);
            event.setCancelled(true);
        }
    }*/

}
