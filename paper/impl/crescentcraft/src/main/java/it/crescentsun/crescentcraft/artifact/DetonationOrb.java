package it.crescentsun.crescentcraft.artifact;

import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.artifacts.item.tooltip.TooltipBuilder;
import it.crescentsun.api.artifacts.item.tooltip.TooltipStyle;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.sound.CompositeSoundEffect;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.inventory.ItemStack;

import static it.crescentsun.api.artifacts.item.tooltip.Tooltip.createHeader;

public class DetonationOrb extends Artifact {

    public static final int RADIUS = 2;
    public static final int DAMAGE = 20;
    public static final int FIRE_TICKS = 160;

    private final SoundEffect placeSound;
    private final CompositeSoundEffect readySound;
    private final SoundEffect ambientSound;
    private final SoundEffect explodeSound;

    protected DetonationOrb(CrescentPlugin plugin, ArtifactFlag... defaultFlags) {
        super(
                plugin,
                ArtifactNamespacedKeys.DETONATION_ORB,
                new ItemStack(Material.FIRE_CHARGE, 1),
                "<@red>Detonation Orb </@>",
                TooltipStyle.DEFAULT,
                defaultFlags
        );
        placeSound = new SoundEffect(plugin, Sound.BLOCK_CORAL_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 0.5f);
        readySound = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_MAGMA_CUBE_DEATH, SoundCategory.PLAYERS, 0.75f, 0.5f),
                new SoundEffect(plugin, Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 0.75f, 1.2f));
        ambientSound = new SoundEffect(plugin, Sound.BLOCK_FIRE_AMBIENT, SoundCategory.AMBIENT, 0.6f, 1.45f);
        explodeSound = new SoundEffect(plugin, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 1.2f);
    }

    @Override
    protected void createTooltip() {
        tooltip = TooltipBuilder.builder()
                .page()
                    .section(createHeader("ATTRIBUTES", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getPrimaryHex1() + "Stacks up to 16")
                    .endSection()
                    .section(createHeader("DESCRIPTION", tooltipStyle.getHeaderHex1()))
                        .addLine(tooltipStyle.getSecondaryHex1() + "May be placed on a surface to")
                        .addLine(tooltipStyle.getSecondaryHex1() + "detonate after a short delay,")
                        .addLine(tooltipStyle.getSecondaryHex1() + "dealing damage and setting")
                        .addLine(tooltipStyle.getSecondaryHex1() + "nearby entities on fire.")
                    .endSection()
                .endPage()
                .page()
                    .section(createHeader("STATS", tooltipStyle.getHeaderHex2()))
                        .addLine(tooltipStyle.getPrimaryHex2() + "Radius: " + RADIUS + " blocks")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Damage: " + DAMAGE + " HP")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Fire Duration: " + (FIRE_TICKS / 20) + " seconds")
    }
}
