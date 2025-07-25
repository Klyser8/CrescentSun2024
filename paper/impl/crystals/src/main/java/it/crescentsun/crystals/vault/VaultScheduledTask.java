package it.crescentsun.crystals.vault;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;

import java.util.Optional;
import java.util.function.Consumer;

public class VaultScheduledTask implements Consumer<BukkitTask> {

    private final Crystals plugin;
    private final BlockDisplay vaultEntity;
    private final TextDisplay textDisplay;
    private final Player owner;
    private final VaultData vaultData;
    private int tickCounter;

    public VaultScheduledTask(Crystals plugin, Player owner, VaultData vaultData) {
        this.plugin = plugin;
        this.owner = owner;
        this.vaultData = vaultData;

        vaultEntity = owner.getWorld().spawn(vaultData.getLocation().add(0.25, 2.0, 0.25), BlockDisplay.class, blockDisplay -> {
            blockDisplay.setBlock(Material.BEACON.createBlockData());
            blockDisplay.setGravity(false);
            Transformation t = blockDisplay.getTransformation();
            t.getScale().set(0.5f, 0.5f, 0.5f);
            blockDisplay.setTransformation(t);
        });
        textDisplay = owner.getWorld().spawn(vaultData.getLocation().add(0.5, 2.5, 0.5), TextDisplay.class, text -> {
            Optional<Integer> crystalsInVault = plugin.getPlayerDataService().getData(owner).getDataValue(
                    DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
            text.setBillboard(Display.Billboard.CENTER);
            text.setGravity(false);
            text.setViewRange(0.05f); //
            text.setVisibleByDefault(false);
            owner.showEntity(plugin, text);
            int crystalsInVaultValue = crystalsInVault.orElse(0);
            text.text(MiniMessage.miniMessage().deserialize(CrescentHexCodes.ICE_CITADEL + "Crystal Vault" + CrescentHexCodes.WHITE + " - " + CrescentHexCodes.DROPLET + crystalsInVaultValue));
        });
    }

    @Override
    public void accept(BukkitTask bukkitTask) {

    }
}