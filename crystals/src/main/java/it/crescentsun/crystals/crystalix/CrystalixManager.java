package it.crescentsun.crystals.crystalix;

import it.crescentsun.crystals.Crystals;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class CrystalixManager {

    private final Crystals plugin;
    private final Map<Player, CrystalixEntity> crystalixMap = new HashMap<>();

    public CrystalixManager(Crystals plugin) {
        this.plugin = plugin;
    }

    public CrystalixEntity createCrystalix(Player player) {
        return crystalixMap.put(player, new CrystalixEntity(plugin, player));
    }

    public CrystalixEntity getCrystalix(Player player) {
        return crystalixMap.get(player);
    }

    public void removeCrystalix(Player player) {
        CrystalixEntity crystalix = crystalixMap.get(player);
        if (crystalix == null) {
            return;
        }
        crystalix.delete();
        crystalixMap.remove(player);
    }
}
