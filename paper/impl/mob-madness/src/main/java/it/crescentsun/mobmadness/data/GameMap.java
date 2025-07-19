package it.crescentsun.mobmadness.data;

import it.crescentsun.mobmadness.game.MapType;

/**
 * Stores data related to the map being played in the game.
 */
public class GameMap {

    private final MapType mapType;

    public GameMap(MapType mapType) {
        this.mapType = mapType;
    }

    public MapType getMapType() {
        return mapType;
    }
}
