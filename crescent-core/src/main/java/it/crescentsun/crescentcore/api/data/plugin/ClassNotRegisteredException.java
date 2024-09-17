package it.crescentsun.crescentcore.api.data.plugin;

/**
 * Exception thrown when a class is not registered with the PluginDataRegistry.
 */
public class ClassNotRegisteredException extends RuntimeException {
    public ClassNotRegisteredException(String message) {
        super(message);
    }
}
