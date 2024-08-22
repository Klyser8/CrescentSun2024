package it.klynet.klynetcore.plugindata;

/**
 * This class is used to store additional data of any type.
 * See {@link PluginDataRegistry} for more information.
 * @param <V>
 */
public class PluginData<V> {
    private V value;
    private DataType type;

    /**
     * Constructor for AdditionalData.
     *
     * @param type The type of the data ({@link DataType})
     * @param value The value to store
     */
    protected PluginData(DataType type, V value) {
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
    public PluginData(PluginData<V> original) {
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
        return "PluginData{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}