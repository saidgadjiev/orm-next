package ru.saidgadjiev.ormnext.core.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is used to specify a mapped column for a persistent property or field.
 *
 * @author Said Gadjiev
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseColumn {

    /**
     * Default column length.
     */
    int DEFAULT_LENGTH = 255;

    /**
     * The name of the column. Defaults to the field name.
     *
     * @return column name
     */
    String columnName() default "";

    /**
     * Column type.
     *
     * @return column type
     * @see DataType
     */
    DataType dataType() default DataType.OTHER;

    /**
     * Column length.
     *
     * @return column length
     */
    int length() default DEFAULT_LENGTH;

    /**
     * Not null constraint.
     *
     * @return not null
     */
    boolean notNull() default false;

    /**
     * True if this column is id.
     *
     * @return true if this column is id
     */
    boolean id() default false;

    /**
     * True if this column is generated.
     *
     * @return true if this column is generated
     */
    boolean generated() default false;

    /**
     * (Optional) Whether the column is included in SQL INSERT statements generated by the persistence provider.
     *
     * @return true if this column insertable
     */
    boolean insertable() default true;

    /**
     * (Optional) Whether the column is included in SQL UPDATE statements generated by the persistence provider.
     *
     * @return true if this column updatable
     */
    boolean updatable() default true;

    /**
     * (Optional) The SQL fragment that is used when generating the DEFAULT part for the column.
     *
     * @return default definition
     */
    String defaultDefinition() default "";

    /**
     * True for set NULL instead of default definition value {@link #defaultDefinition}.
     *
     * @return true for set NULL instead of default definition
     */
    boolean defaultIfNull() default true;

    /**
     * True if column should be unique.
     *
     * @return true if column should be unique
     */
    boolean unique() default false;

    /**
     * If true column will be defined in create table.
     *
     * @return if true column will be defined in create table
     */
    boolean defineInCreateTable() default true;
}
