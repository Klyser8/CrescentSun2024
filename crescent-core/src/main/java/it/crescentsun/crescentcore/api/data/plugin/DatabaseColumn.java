package it.crescentsun.crescentcore.api.data.plugin;

import it.crescentsun.crescentcore.api.data.DataType;

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
    String columnName();

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

    int order();
}