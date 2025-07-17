package it.crescentsun.api.crescentcore.data;

import java.sql.Timestamp;

/**
 * Enum for the different data types that can be registered through the PlayerDataEntryRegistry.
 * It is also used to create SQL tables for player and plugin data.
 */
public enum DataType {

    BOOLEAN(Boolean.class, "BOOLEAN NOT NULL"),
    INT(Integer.class, "INT NOT NULL DEFAULT 0"),
    UNSIGNED_INT(Integer.class, "INT UNSIGNED NOT NULL DEFAULT 0"),
    BIG_INT(Long.class, "BIGINT NOT NULL DEFAULT 0"),
    UNSIGNED_BIG_INT(Long.class, "BIGINT UNSIGNED NOT NULL DEFAULT 0"),
    DOUBLE(Double.class, "DOUBLE NOT NULL DEFAULT 0.0"),
    FLOAT(Float.class, "FLOAT NOT NULL DEFAULT 0.0"),
    VARCHAR_16(String.class, "VARCHAR(16) NOT NULL"),
    NULLABLE_VARCHAR_16(String.class, "VARCHAR(16)"),
    VARCHAR_36(String.class, "VARCHAR(36) NOT NULL"),
    NULLABLE_VARCHAR_36(String.class, "VARCHAR(36)"),
    VARCHAR_255(String.class, "VARCHAR(255) NOT NULL"),
    NULLABLE_VARCHAR_255(String.class, "VARCHAR(255)"),
    TIMESTAMP(Timestamp.class, "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");

    private final Class<?> clazz;
    private final String sqlType;
    <T> DataType(Class<T> clazz, String sqlType) {
        this.clazz = clazz;
        this.sqlType = sqlType;
    }

    /**
     * Get the class of the data type.
     * @return The class of the data type
     */
    public Class<?> getTypeClass() {
        return clazz;
    }

    /**
     * Get the SQL type of the data type.
     * Used for creating tables and columns in the database.
     * @return The SQL type of the data type
     */
    public String getSqlType() {
        return sqlType;
    }

    /**
     * Get the data type from an object.
     * @param value The object to get the data type from
     * @return The data type of the object, or null if the object is not a valid data type
     */
    public static DataType fromObject(Object value) {
        for (DataType type : values()) {
            if (type.clazz.isInstance(value)) {
                return type;
            }
        }
        return null;
    }
}
