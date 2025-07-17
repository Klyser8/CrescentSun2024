package it.crescentsun.api.crescentcore.data.plugin;

/**
 * Represents a singleton plugin data class.
 * A singleton plugin data class does <b>not</b> require a PluginDataManager implementation, as only ever one instance per backend server is going to be present.
 * @see PluginData
 */
public abstract class SingletonPluginData extends PluginData {

    public SingletonPluginData() {
        super();
    }

    /**
     * Since singleton plugin data classes are only ever going to have one instance per backend server, this method should always return true.
     */
    @Override
    public boolean shouldInit() {
        return true;
    }
}
