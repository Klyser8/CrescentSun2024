package it.crescentsun.crystals.vault;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Consumer;

public class VaultScheduledTask implements Consumer<BukkitTask> {

    private final Crystals plugin;
    private final BlockDisplay vaultEntity;
    private final TextDisplay textDisplay;
    private final Player owner;
    private final VaultData vaultData;
    private int tickCounter;
    private Matrix4f vaultMatrix;

    public VaultScheduledTask(Crystals plugin, Player owner, VaultData vaultData) {
        this.plugin = plugin;
        this.owner = owner;
        this.vaultData = vaultData;

        vaultEntity = owner.getWorld().spawn(vaultData.getLocation().add(0.5, 2.0, 0.5), BlockDisplay.class, blockDisplay -> {
            blockDisplay.setBlock(Material.BEACON.createBlockData());
            blockDisplay.setGravity(false);
//            blockDisplay.setInterpolationDelay(1);
//            blockDisplay.setInterpolationDuration(1);
            vaultMatrix = new Matrix4f()
                    .scale(0.5f, 0.5f, 0.5f)
                    .translate(-0.5f, 0f, -0.5f);
            blockDisplay.setTransformationMatrix(vaultMatrix);
        });
        textDisplay = owner.getWorld().spawn(vaultData.getLocation().add(0.5, 2.5, 0.5), TextDisplay.class, text -> {
            Optional<Integer> crystalsInVault = plugin.getPlayerDataService().getData(owner).getDataValue(
                    DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
            text.setBillboard(Display.Billboard.CENTER);
            text.setGravity(false);
            text.setViewRange(0.05f); //
            text.setVisibleByDefault(false);
//            text.setInterpolationDelay(1);
//            text.setInterpolationDuration(1);
            owner.showEntity(plugin, text);
            int crystalsInVaultValue = crystalsInVault.orElse(0);
            text.text(MiniMessage.miniMessage().deserialize(CrescentHexCodes.ICE_CITADEL + "Crystal Vault" + CrescentHexCodes.WHITE + " - " + CrescentHexCodes.DROPLET + crystalsInVaultValue));
        });
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        tickCounter++;

        float animationProgress = (float) tickCounter / 40.0f;
        float yOffset = 0.1f * (float) Math.sin(animationProgress * Math.PI);
        float angleRad = (float) Math.toRadians((tickCounter * 0.5) % 360);

        vaultEntity.setInterpolationDelay(0);
        vaultEntity.setInterpolationDuration(3);
        vaultEntity.setTransformationMatrix(
                new Matrix4f()
                        .translate(0, yOffset, 0)
                        .rotateY(angleRad)
                        .mul(vaultMatrix)
        );

        textDisplay.setInterpolationDelay(0);
        textDisplay.setInterpolationDuration(3);
        Transformation textTrans = textDisplay.getTransformation();
        textTrans.getTranslation().set(0.0f, yOffset, 0.0f);
        textDisplay.setTransformation(textTrans);
    }
}