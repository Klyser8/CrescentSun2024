package it.crescentsun.api.crescentcore.data.plugin;

import java.util.UUID;

/**
 * Represents a unique identifier for a {@link PluginData} object.
 * @param classType The class type of the {@link PluginData} object
 * @param uuid The unique identifier of the PluginData instance
 * @param <T> The type of the PluginData object
 */
public record PluginDataIdentifier<T extends PluginData>(Class<T> classType, UUID uuid) { }