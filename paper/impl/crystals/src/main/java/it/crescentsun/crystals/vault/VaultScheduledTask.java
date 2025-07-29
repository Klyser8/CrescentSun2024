package it.crescentsun.crystals.vault;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class VaultScheduledTask implements Consumer<BukkitTask> {

    private final Crystals plugin;
    private final BlockDisplay vaultEntity;
    final TextDisplay textDisplay;
    private final BlockDisplay[] lanterns = new BlockDisplay[2];
    private final Interaction interactionEntity;
    private final Matrix4f vaultMatrix;
    private final Matrix4f lanternBaseMatrix;
    private final Player owner;
    private final VaultData vaultData;

    // for dynamic orbit radius
    private float currentLanternRadius = 0f;
    private static final float TARGET_LANTERN_RADIUS = 0.6f;
    private static final float RADIUS_STEP = 0.02f;

    private int tickCounter;

    public VaultScheduledTask(Crystals plugin, Player owner, VaultData vaultData) {
        this.plugin    = plugin;
        this.owner     = owner;
        this.vaultData = vaultData;

        // BASE MATRIX for the big vault block (half-size & centered)
        vaultMatrix = new Matrix4f()
                .scale(0.5f, 0.5f, 0.5f)
                .translate(-0.5f, 0f, -0.5f);

        // BASE MATRIX for the small lanterns (10% size, centered)
        lanternBaseMatrix = new Matrix4f()
                .scale(0.33f, 0.33f, 0.33f)
                .translate(-0.5f, -0.5f, -0.5f);

        // 1) Vault BlockDisplay
        World world = owner.getWorld();
        Location vaultOrigin = vaultData.getLocation().add(0.5, 2.0, 0.5);
        vaultEntity = world.spawn(
                vaultOrigin,
                BlockDisplay.class, bd -> {
                    bd.setBlock(Material.BEACON.createBlockData());
                    bd.setGravity(false);
                    bd.setTransformationMatrix(vaultMatrix);
                    bd.setMetadata(VaultData.VAULT_KEY.getKey(), new FixedMetadataValue(plugin, vaultData.getUuid()));
                }
        );

        // 2) TextDisplay
        textDisplay = world.spawn(
                vaultData.getLocation().add(0.5, 2.5, 0.5),
                TextDisplay.class, td -> {
                    td.setBillboard(Display.Billboard.CENTER);
                    td.setGravity(false);

                    // Public vaults should be visible from a distance and have a different title
                    if (vaultData.isPublic()) {
                        td.setVisibleByDefault(true);
                        td.text(MiniMessage.miniMessage().deserialize(CrescentHexCodes.ICE_CITADEL + "Public Crystal Vault"));
                    } else {
                        td.setViewRange(0.05f);
                        td.setVisibleByDefault(false);
                        owner.showEntity(plugin, td);
                    }
                }
        );

        // 3) Two orbiting lanterns, initially invisible at center
        for (int i = 0; i < lanterns.length; i++) {
            lanterns[i] = world.spawn(
                    vaultOrigin,
                    BlockDisplay.class, ld -> {
                        ld.setBlock(Material.CONDUIT.createBlockData());
                        ld.setGravity(false);
                        ld.setInterpolationDelay(0);
                        ld.setInterpolationDuration(3);
                        ld.setTransformationMatrix(lanternBaseMatrix);
                        ld.setMetadata(VaultData.VAULT_KEY.getKey(), new FixedMetadataValue(plugin, vaultData.getUuid()));
                    }
            );
        }

        interactionEntity = world.spawn(vaultOrigin.add(0, -0.12, 0), Interaction.class, entity -> {
            entity.setInteractionHeight(0.75f);
            entity.setInteractionWidth(0.6f);
            entity.setResponsive(true);
            entity.setGravity(false);
            entity.setMetadata(VaultData.VAULT_KEY.getKey(), new FixedMetadataValue(plugin, vaultData.getUuid()));
        });
    }

    @Override
    public void accept(BukkitTask task) {
        tickCounter++;

        // cleanup if invalid
        if (!vaultEntity.isValid() || !textDisplay.isValid()
                || !lanterns[0].isValid() || !lanterns[1].isValid() || !interactionEntity.isValid() ||
                plugin.getVaultManager().getDataInstance(vaultData.getUuid()) == null) {
            task.cancel();
            vaultEntity.remove();
            textDisplay.remove();
            lanterns[0].remove();
            lanterns[1].remove();
            interactionEntity.remove();
            return;
        }

        // compute player distance horizontally
        Location vaultLoc = vaultData.getLocation().add(0.5, 2.0, 0.5);
        double distance = owner.getLocation().distance(vaultLoc);

        // adjust lantern radius and visibility based on distance
        if (distance <= 5) {
            // player is close: ramp up towards target
            currentLanternRadius = Math.min(TARGET_LANTERN_RADIUS,
                    currentLanternRadius + RADIUS_STEP);
            // show lanterns
            for (BlockDisplay ld : lanterns) {
                ld.setInvisible(false);
            }
        } else {
            // player is far: ramp down towards center
            currentLanternRadius = Math.max(0f,
                    currentLanternRadius - RADIUS_STEP);
            // hide when fully retracted
            if (currentLanternRadius <= 0f) {
                for (BlockDisplay ld : lanterns) {
                    ld.setInvisible(true);
                }
            }
        }

        // common bob value
        float prog    = tickCounter / 40f;
        float yOffset = 0.1f * (float)Math.sin(prog * Math.PI);

        // — vault bob & spin —
        vaultEntity.setInterpolationDelay(0);
        vaultEntity.setInterpolationDuration(3);
        Matrix4f newVaultMatrix = new Matrix4f()
                    .translate(0, yOffset, 0)
                    .rotateY((float) Math.toRadians(tickCounter * 0.5))
                    .mul(vaultMatrix);
        vaultEntity.setTransformationMatrix(
                newVaultMatrix
        );

        // — text bob —
        Transformation tt = textDisplay.getTransformation();
        tt.getTranslation().set(0, yOffset, 0);
        textDisplay.setInterpolationDelay(0);
        textDisplay.setInterpolationDuration(3);
        textDisplay.setTransformation(tt);

        // — orbiting lanterns —
        if (currentLanternRadius > 0f) {
            float orbBase = (float)Math.toRadians((tickCounter * 2) % 360);
            for (int i = 0; i < lanterns.length; i++) {
                float angle = orbBase + i * (float)Math.PI;
                float xOff  = (float)Math.cos(angle) * currentLanternRadius;
                float zOff  = (float)Math.sin(angle) * currentLanternRadius;

                lanterns[i].setInterpolationDelay(0);
                lanterns[i].setInterpolationDuration(3);
                lanterns[i].setTransformationMatrix(
                        new Matrix4f()
                                .translate(xOff, yOffset + 0.25f, zOff)
                                .rotateY(- (float)Math.toRadians(tickCounter * 4))
                                .mul(lanternBaseMatrix)
                );
            }
        }

        // — particles only when player is close —
        if (distance <= 5) {
            Location origin = vaultEntity.getLocation().clone().add(0, yOffset + 0.25, 0);
            World world = origin.getWorld();
            // use same base orbit as lanterns, but pull back by a small lag
            float lagAngle = 0.1f; // radians to trail behind
            float baseOrbit = (float)Math.toRadians((tickCounter * 2) % 360) - lagAngle;
            float spawnRadius = currentLanternRadius; // match lanterns

            // two mycelium orbs, trailing behind each lantern
            for (int i = 0; i < 2; i++) {
                double angle = baseOrbit + (i * Math.PI);
                double x = Math.cos(angle) * spawnRadius;
                double z = Math.sin(angle) * spawnRadius;
                world.spawnParticle(Particle.MYCELIUM,
                        origin.clone().add(x, 0, z),
                        1, 0, 0, 0, 0);
            }
            Random random = plugin.random();
            if (random.nextDouble() < 0.1) { //dsdasdsad
                world.spawnParticle(Particle.OMINOUS_SPAWNING,
                        origin, 1);
            }
            if (random.nextDouble() < 0.15) {
                for (int i = 0; i < 8; i++) {
                    double xOffset, zOffset;
                    if (i % 2 == 0) {
                        xOffset = (random.nextDouble() - 0.5) * 3;
                        zOffset = (random.nextDouble() - 0.5) / 6;
                        if (Math.abs(xOffset) < 0.4) continue;
                    } else {
                        xOffset = (random.nextDouble() - 0.5) / 6;
                        zOffset = (random.nextDouble() - 0.5) * 3;
                        if (Math.abs(zOffset) < 0.4) continue;
                    }
                    world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                            origin.clone().add(xOffset, -2, zOffset),
                            1, 0, 0, 0, 0);
                }
            }
        }
    }

}
