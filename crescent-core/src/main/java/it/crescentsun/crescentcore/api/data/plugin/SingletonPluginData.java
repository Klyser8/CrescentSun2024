package it.crescentsun.crescentcore.api.data.plugin;

/**
 * Represents a singleton plugin data class.
 * A singleton plugin data class does not require a PluginDataManager implementation, as only ever one instance per backend server is going to be present.
 */
public abstract class SingletonPluginData extends PluginData {

    public SingletonPluginData() {
        super();
    }

    @Override
    public boolean shouldInit() {
        return true;
    }
}
