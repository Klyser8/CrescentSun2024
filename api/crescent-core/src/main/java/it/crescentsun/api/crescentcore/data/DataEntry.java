package it.crescentsun.api.crescentcore.data;

import it.crescentsun.api.crescentcore.data.player.PlayerData;
import org.jetbrains.annotations.ApiStatus;

/**
 * This class is used to store additional data of any type.
 * Used within the context of {@link PlayerData}
 * @param <V> The type of the data being stored
 */
public class DataEntry<V> {
    private V value;
    private final DataType type;

    /**
     * Constructor for AdditionalData.
     *
     * @param type The type of the data ({@link DataType})
     * @param value The value to store
     * @param persistent Whether the data should be stored in the database or deleted on server shutdown
     */
    @ApiStatus.Internal
    public DataEntry(DataType type, V value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Copy constructor for AdditionalData.
     * Created to avoid sharing references to the same object.
     * Allows to create a deep copy of the original AdditionalData.
     *
     * @param original The original AdditionalData to copy
     */
    public DataEntry(DataEntry<V> original) {
        this.type = original.type;
        this.value = original.value; // Direct copy, assumes V is immutable or you're okay with shared references
    }


    /**
     * Returns the type of the data being stored.
     * @return The type of the data.
     */
    public DataType getType() {
        return type;
    }


    /**
     * Returns the value of the data.
     * @return The value of the data
     */
    public V getValue() {
        return this.value;
    }

    /**
     * Sets the value of the data.
     * @param value The value to set
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        this.value = (V) value;
    }

    @Override
    public String toString() {
        return "PlayerDataEntry{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}