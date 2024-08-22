package it.klynet.klynetcore.api;

import it.klynet.klynetcore.api.event.crystals.CrystalGenerationSource;
import org.bukkit.entity.Player;

public interface CrystalsProvider {

    void spawnCrystals(Player player, int amount, CrystalGenerationSource spawnReason);
    void addCrystals(Player player, int amount);
    void removeCrystals(Player player, int amount);
    void setCrystals(Player player, int amount);
    int getCrystals(Player player);

}
