package it.crescentsun.crescentcore.api;

import it.crescentsun.crescentcore.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.crescentcore.api.crystals.event.CrystalSource;
import org.bukkit.entity.Player;

public interface CrystalsProvider {

    void spawnCrystals(Player player, int amount, CrystalSource crystalSource, CrystalSpawnAnimation spawnAnimation);
    void addCrystals(Player player, int amount, CrystalSource crystalSource);
    void removeCrystals(Player player, int amount, CrystalSource crystalSource);
    void setCrystals(Player player, int amount, CrystalSource crystalSource);
    int getCrystals(Player player);

}
