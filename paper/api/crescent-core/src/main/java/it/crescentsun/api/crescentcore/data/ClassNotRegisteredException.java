package it.crescentsun.api.crescentcore.data;

import it.crescentsun.api.crescentcore.data.plugin.PluginData;

/**
 * Exception meant to be thrown when a class is not registered.
 * The original context has to do with {@link PluginData}, though it is part of the API for possible use elsewhere.
 */
public class ClassNotRegisteredException extends RuntimeException {
    public ClassNotRegisteredException(String message) {
        super(message);
    }
}
