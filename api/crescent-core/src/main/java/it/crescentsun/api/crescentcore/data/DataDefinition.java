package it.crescentsun.api.crescentcore.data;

/**
 * This class defines the structure of data entries, including their type, default value, and persistence.
 * It is used to specify the characteristics of data that can be stored into the database.
 *
 * @param <V> The type of the data being defined.
 */
public class DataDefinition<V> {
    private V defaultValue;
    private final DataType type;
    private final boolean persistent;

    /**
     * Constructs a new DataDefinition with the specified type, default value, and persistence.
     *
     * @param type The type of the data ({@link DataType}).
     * @param defaultValue The default value of the data.
     * @param persistent Whether the data should be stored in the database or deleted on server shutdown.
     */
    public DataDefinition(DataType type, V defaultValue, boolean persistent) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.persistent = persistent;
    }

    /**
     * Copy constructor for DataDefinition.
     * Creates a new DataDefinition by copying the properties of an existing one.
     *
     * @param original The original DataDefinition to copy.
     */
    public DataDefinition(DataDefinition<V> original) {
        this.type = original.type;
        this.defaultValue = original.defaultValue;
        this.persistent = original.persistent;
    }

    /**
     * Returns the type of the data being defined.
     *
     * @return The type of the data.
     */
    public DataType getType() {
        return type;
    }

    /**
     * Returns the default value of the data.
     *
     * @return The default value of the data.
     */
    public V getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns whether the data being defined is persistent.
     * Persistent data is stored in the database and is not deleted on server shutdown.
     *
     * @return Whether the data definition is persistent.
     */
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public String toString() {
        return "PlayerDataEntry{" +
                "defaultValue=" + defaultValue +
                ", type=" + type +
                '}';
    }
}