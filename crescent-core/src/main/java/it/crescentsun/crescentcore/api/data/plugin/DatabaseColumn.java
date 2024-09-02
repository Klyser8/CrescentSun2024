package it.crescentsun.crescentcore.api.data.plugin;

import it.crescentsun.crescentcore.api.data.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseColumn {
    String columnName();
    DataType dataType();
    boolean isPrimaryKey() default false;
}
