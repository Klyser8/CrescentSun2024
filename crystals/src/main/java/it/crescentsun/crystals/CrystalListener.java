package it.crescentsun.crystals;

import it.crescentsun.artifacts.api.ArtifactUtil;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.crescentsun.crystals.artifact.CrystalArtifact;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.CompletableFuture;

public class CrystalListener implements Listener {

    private final Crystals plugin;

    public CrystalListener(Crystals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        AdvancementDisplay display = event.getAdvancement().getDisplay();
        if (display == null) {
            return;
        }
        AdvancementDisplay.Frame frame = display.frame();
        if (frame.equals(AdvancementDisplay.Frame.CHALLENGE)) {

        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) { //TODO crystalix upon collecting a crystal, stores its id 
        Item entity = event.getEntity();
        ItemStack stack = entity.getItemStack();
        if (!(ArtifactUtil.identifyArtifact(stack) instanceof CrystalArtifact crystalArtifact)) {
            return;
        }
    }

    @EventHandler
    public void onServerLoadPostDB(ServerLoadPostDBSetupEvent event) { //TODO test singleton
        plugin.setCrystalsData(plugin.getCrescentCore().getPluginDataRepository().getData(CrystalsData.class, CrystalsData.UUID));
    }

}
