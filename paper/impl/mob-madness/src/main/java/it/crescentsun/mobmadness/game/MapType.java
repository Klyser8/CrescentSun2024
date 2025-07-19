package it.crescentsun.mobmadness.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum MapType {

    VILLAGE(0, Component.text("Village"), new Location(Bukkit.getWorld("world"), 0, 100, 0), 0.75f, 1.25f), // Forest
    LAKESIDE_VIEW(1, Component.text("Lakeside View"), new Location(Bukkit.getWorld("world"), 0, 100, 0), 1.0f, 1.00f), // Forest + lake
    QUARRY(2, Component.text("Quarry"), new Location(Bukkit.getWorld("world"), 0, 100, 0), 1.25f, 0.80f), // Mountainous area with a quarry
    FROZEN_PEAKS(3, Component.text("Frozen Peaks"), new Location(Bukkit.getWorld("world"), 0, 100, 0), 1.25f, 0.80f), // Snowy mountains with ice structures
    ANCIENT_RUINS(4, Component.text("Ancient Ruins"), new Location(Bukkit.getWorld("world"), 0, 100, 0), 1.5f, 0.66f); // Desert ruins with sand and stone structures

    final int mapId;
    final TextComponent mapName;
    final Location spawnLocation;
    final float weightMultiplier;
    final float boundMultiplier;

    /**
     * Constructor for the MapType enum.
     * @param mapId unique identifier for the map
     * @param mapName name of the map, which shows up in-game
     * @param spawnLocation the location where players spawn when the game starts. Also, the location of the respawn anchor.
     * @param weightMultiplier multiplier for the maximum weight of mobs that can spawn in this map. Higher values mean more mobs can be spawned.
     * @param boundMultiplier multiplier for the bound of the game, which dictates the amount of mobs that spawn. Lower multiplier means mobs spawn more often.
     */
    MapType(int mapId, TextComponent mapName, Location spawnLocation, float weightMultiplier, float boundMultiplier) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.spawnLocation = spawnLocation;
        this.weightMultiplier = weightMultiplier;
        this.boundMultiplier = boundMultiplier;
    }

    public int getMapId() {
        return mapId;
    }

    public TextComponent getMapName() {
        return mapName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public float getWeightMultiplier() {
        return weightMultiplier;
    }

    public float getBoundMultiplier() {
        return boundMultiplier;
    }
}
