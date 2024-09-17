package it.crescentsun.crystals.crystalix.listener;

import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.crescentcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.crescentsun.crystals.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

@Deprecated
public class CrystalixListener implements Listener {

    private final Crystals plugin;

    public CrystalixListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDataLoadedPostJoin(PlayerJoinEventPostDBLoad event) {
        Player player = event.getPlayer();
        PlayerData playerData = event.getPlayerData();
        if (playerData == null) {
            return;
        }
        /*if (playerData.getData(CrescentNamespaceKeys.SETTINGS_SHOW_CRYSTALIX)) {
            plugin.getCrystalixManager().createCrystalix(player);
        }*/
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
//        if (plugin.getCrystalixManager().getCrystalix(player) == null) {
//            return;
//        }
//        Bukkit.getLogger().info("Removing crystalix for " + player.getName());
//        plugin.getCrystalixManager().removeCrystalix(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerLoadPostDB(ServerLoadPostDBSetupEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
//            PlayerData data = PlayerUtils.getPlayerData(player);
            /*if (data.getData(CrescentNamespaceKeys.SETTINGS_SHOW_CRYSTALIX)) {
                plugin.getCrystalixManager().createCrystalix(player);
            }*/
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
