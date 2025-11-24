package it.crescentsun.crescentcraft.artifact;

import it.crescentsun.api.artifacts.event.ArtifactInteractEvent;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.artifacts.item.tooltip.TooltipBuilder;
import it.crescentsun.api.artifacts.item.tooltip.TooltipStyle;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.crescentcraft.CrescentCraft;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static it.crescentsun.api.artifacts.item.tooltip.Tooltip.createHeader;

public class DetonationOrb extends Artifact {

    public static final float RADIUS = 1.25f;
    public static final int DAMAGE = 20;
    public static final int FIRE_TICKS = 160;

    public DetonationOrb(CrescentPlugin plugin, ArtifactFlag... defaultFlags) {
        super(
                plugin,
                ArtifactNamespacedKeys.DETONATION_ORB,
                new ItemStack(Material.FIRE_CHARGE, 1),
                "<#ff0000>Detonation Orb",
                TooltipStyle.DEFAULT,
                defaultFlags
        );

        defaultMeta.setMaxStackSize(16);
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
                        .addLine(tooltipStyle.getPrimaryHex2() + "Radius: {" + RADIUS + "} blocks")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Damage: {" + DAMAGE + "} HP")
                        .addLine(tooltipStyle.getPrimaryHex2() + "Fire Duration: {" + (FIRE_TICKS / 20) + "} seconds")
                    .endSection()
                .endPage()
            .build();
    }

    @Override
    public boolean interactRight(ArtifactInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return false;
        }
        Block clickedBlock = event.getClickedBlock();

        if (isUnderwater(clickedBlock)) {
            return false;
        }

        PlacementResult placement = resolvePlacement(clickedBlock, event.getClickedBlockFace());
        if (!placement.canPlace()) {
            return false;
        }

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack itemInHand = inventory.getItem(event.getHand());
        if (itemInHand == null) {
            return false;
        }

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        ((CrescentCraft) plugin).getDetonationOrbManager().placeOrb(player, placement.location());
        return true;
    }

    private PlacementResult resolvePlacement(Block clickedBlock, BlockFace clickedFace) {
        if (clickedBlock == null) {
            return PlacementResult.failed();
        }

        BlockFace targetFace = clickedFace != null ? clickedFace : BlockFace.UP;
        boolean clickedReplaceable = isReplaceable(clickedBlock);
        Block targetBlock = clickedReplaceable ? clickedBlock : clickedBlock.getRelative(targetFace);

        if (isUnderwater(targetBlock)) {
            return PlacementResult.failed();
        }

        if (!(targetBlock.isEmpty() || isReplaceable(targetBlock))) {
            return PlacementResult.failed();
        }

        if (!hasSupport(targetBlock)) {
            return PlacementResult.failed();
        }

        return PlacementResult.success(targetBlock.getLocation());
    }

    private boolean hasSupport(Block targetBlock) {
        return !targetBlock.getRelative(BlockFace.DOWN).isPassable();
    }

    private boolean isUnderwater(Block block) {
        if (block == null) {
            return true;
        }
        if (block.getType() == Material.WATER || block.isLiquid()) {
            return true;
        }
        if (block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
            return true;
        }
        return false;
    }

    private boolean isReplaceable(Block block) {
        if (block == null) {
            return false;
        }
        return block.isPassable() && !isUnderwater(block);
    }

    private record PlacementResult(boolean canPlace, Location location) {
        static PlacementResult failed() {
            return new PlacementResult(false, null);
        }

        static PlacementResult success(Location location) {
            return new PlacementResult(true, location);
        }
    }
}
