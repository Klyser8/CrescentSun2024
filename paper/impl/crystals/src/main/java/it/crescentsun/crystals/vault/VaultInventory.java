package it.crescentsun.crystals.vault;

import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VaultInventory implements InventoryHolder {

    ///Inventory title, with placeholder for player name and crystal amount
    private final String vaultInventoryRawString ="<#6e6e6e>%s's Vault - <#ffffff>%d";

    private final Inventory inventory;
    private final Crystals plugin;
    private final Player owner;
    private final UUID vaultUUID;

    public VaultInventory(Crystals plugin, Player owner, UUID vaultUUID) {
        this.plugin = plugin;
        this.owner = owner;
        this.vaultUUID = vaultUUID;

        PlayerData ownerData = plugin.getPlayerDataService().getData(owner.getUniqueId());
        int crystalsInVault = (int) ownerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT).orElse(0);

        String formatted = String.format(vaultInventoryRawString, owner.getName(), crystalsInVault);//
        TextComponent title = (TextComponent) MiniMessage.miniMessage().deserialize(formatted);
        this.inventory = plugin.getServer().createInventory(this, 54, title);

        Artifact crystalArtifact = plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTAL);
        int slots = inventory.getSize();            // 54
        int base = crystalsInVault / slots;    // floor division
        int remainder = crystalsInVault % slots;    // leftover to distribute
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int amount = base + (slot < remainder ? 1 : 0);
            if (amount <= 0) {
                continue;
            }
            inventory.setItem(slot, crystalArtifact.createStack(amount));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Player getOwner() {
        return owner;
    }

    public UUID getVaultUUID() {
        return vaultUUID;
    }
}
