package it.klynet.crystals.artifact;

import it.klynet.artifacts.item.Artifact;
import it.klynet.artifacts.item.ArtifactFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CrystalixArtifact extends Artifact {
    public CrystalixArtifact(NamespacedKey itemKey, ItemStack defaultStack, String displayName, ArtifactFlag... flags) {
        super(itemKey, defaultStack, displayName, flags);
    }

}
