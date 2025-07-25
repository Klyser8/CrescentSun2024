package it.crescentsun.crystals.crystalix;

import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.function.Consumer;

@Deprecated
public class CrystalixEntity {
    public static final double Y_OFFSET = 1.0;
    private final Crystals plugin;
    private final Player owner;
    private final PlayerData ownerData;
    private final World world;
    private Location location;
    protected final Item item;
    private final Consumer<BukkitTask> task;
    private int writtenCrystals = 0;

    public CrystalixEntity(Crystals plugin, Player owner) {
        this.plugin = plugin;
        this.owner = owner;
        ownerData = plugin.getPlayerDataService().getData(owner.getUniqueId());
        world = owner.getWorld();
        location = owner.getLocation().add(0, Y_OFFSET, 0);
        item = spawnCrystalix();
        updateName();
        task = new CrystalixTask(plugin, this);
        scheduleTask();
    }

    public Item spawnCrystalix() {
        Item crystalix = world.dropItem(location, plugin.getArtifactRegistryService().getArtifact(ArtifactNamespacedKeys.CRYSTALIX).createStack(0));
        crystalix.setGravity(false);
        crystalix.setPickupDelay(Integer.MAX_VALUE);
        crystalix.setUnlimitedLifetime(true);
        crystalix.setInvulnerable(true);
        crystalix.setCustomNameVisible(true);
        return crystalix;
    }

    public void delete() {
        item.remove();
    }

    public Item getItem() {
        return item;
    }

    public Player getOwner() {
        return owner;
    }

    public PlayerData getOwnerData() {
        return ownerData;
    }

    private void scheduleTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, task, 1, CrystalixTask.INTERVAL);
    }

    public void updateName() {
        TextColor[] colors = new TextColor[]{
                TextColor.color(255, 204, 243),
                TextColor.color(241, 161, 255),
                TextColor.color(208, 127, 255),
                TextColor.color(189, 92, 255),
                TextColor.color(167, 52, 255),
        };
        Optional<Integer> crystals = ownerData.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_SPAWNED);
        writtenCrystals = crystals.orElse(0);
        String crystalsString = String.valueOf(crystals);
        TextComponent name = Component.text("-[");
        int colorIndex = 0;
        for (char c : crystalsString.toCharArray()) {
            TextComponent charComponent = Component.text(String.valueOf(c));
            charComponent = charComponent.color(colors[colorIndex % colors.length]);
            name = name.append(charComponent);
            colorIndex++;
        }
        name = name.append(Component.text("]-"));
        item.customName(name);
    }

    public int getWrittenCrystals() {
        return writtenCrystals;
    }
}
