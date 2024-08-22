package it.klynet.crystals;

import it.klynet.artifacts.api.ArtifactFactory;
import it.klynet.artifacts.api.ArtifactUtil;
import it.klynet.artifacts.item.Artifact;
import it.klynet.crystals.artifact.CrystalArtifact;
import it.klynet.klynetcore.KlyNetCore;
import it.klynet.klynetcore.api.registry.ArtifactNamespaceKeys;
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
        Integer id = stack.getItemMeta().getPersistentDataContainer().get(CrystalArtifact.CRYSTAL_ID, PersistentDataType.INTEGER);
        if (id == null) {
            return;
        }
        CompletableFuture<Integer> crystalsGenerated = KlyNetCore.getInstance().getDatabaseManager().getServerDataManager().getCrystalsGenerated();
        crystalsGenerated.thenAcceptAsync(crystals -> {
            if (crystals >= id) {
                stack.setAmount(0);
            }
        });
    }

}
