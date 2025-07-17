package it.crescentsun.api.crescentcore.data.plugin;

import it.crescentsun.api.crescentcore.data.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as a column in the database table.
 * This annotation should be used on fields within classes that extend {@link PluginData}.
 *
 * @see PluginData
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseColumn {

    /**
     * Specifies the name of the column in the database.
     *
     * @return The name of the column.
     */
    String columnName(); // Default should be field name

    /**
     * Specifies the data type of the column.
     *
     * @return The data type of the column.
     */
    DataType dataType();

    /**
     * Indicates whether the column is a primary key.
     *
     * @return True if the column is a primary key, false otherwise.
     */
    boolean isPrimaryKey() default false;

    /**
     * Specifies the order of the column in the table.
     * Columns with lower order values will be placed before columns with higher order values.
     * @return The order of the column.
     */
    int order();
}