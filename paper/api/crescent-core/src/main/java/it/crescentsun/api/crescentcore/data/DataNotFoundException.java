package it.crescentsun.api.crescentcore.data;

import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;

/**
 * Represents an exception thrown when data is not found.
 * Strictly tied to {@link PluginData} and {@link PlayerData}
 */
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
