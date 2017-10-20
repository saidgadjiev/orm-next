package ru.said.miami.orm.core.table;

import ru.said.miami.orm.core.cache.core.Cache;
import ru.said.miami.orm.core.field.DBField;
import ru.said.miami.orm.core.field.DBFieldType;
import ru.said.miami.orm.core.field.FieldType;
import ru.said.miami.orm.core.field.ForeignCollectionFieldType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TableInfo<T> {

    private final List<FieldType> fieldTypes;

    private final DBFieldType idField;

    private String tableName;

    private Constructor<T> constructor;

    private TableInfo(Constructor<T> constructor, DBFieldType idField, String tableName, List<FieldType> fieldTypes) {
        this.tableName = tableName;
        this.constructor = constructor;
        this.fieldTypes = fieldTypes;
        this.idField = idField;
    }

    public String getTableName() {
        return tableName;
    }

    public Optional<DBFieldType> getIdField() {
        return Optional.ofNullable(idField);
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public List<DBFieldType> toDBFieldTypes() {
        return fieldTypes.stream()
                .filter(FieldType::isDBFieldType)
                .map(FieldType::getDbFieldType)
                .collect(Collectors.toList());
    }

    public List<ForeignCollectionFieldType> toForeignCollectionFieldTypes() {
        return fieldTypes.stream()
                .filter(FieldType::isForeignCollectionFieldType)
                .map(FieldType::getForeignCollectionFieldType)
                .collect(Collectors.toList());
    }

    public static<T> TableInfo buildTableInfo(Class<T> clazz) throws NoSuchMethodException, NoSuchFieldException {
        if (!clazz.isAnnotationPresent(DBTable.class)) {
            throw new IllegalArgumentException("Class not annotated with DBTable.class");
        }
        List<FieldType> fieldTypes = new ArrayList<>();

        for (Field field: clazz.getDeclaredFields()) {
            FieldType.buildFieldType(field).ifPresent(fieldTypes::add);
        }
        if (fieldTypes.isEmpty()) {
            throw new IllegalArgumentException("No fields have a " + DBField.class.getSimpleName()
                    + " annotation in " + clazz);
        }
        String tableName = clazz.getAnnotation(DBTable.class).name();
        TableInfo<T> tableInfo = new TableInfo<>(
                lookupDefaultConstructor(clazz),
                getIdGeneratedIdField(fieldTypes),
                tableName.isEmpty() ? clazz.getSimpleName().toLowerCase() : tableName, fieldTypes);

        return tableInfo;
    }


    private static DBFieldType getIdGeneratedIdField(List<FieldType> fieldTypes) {
        return fieldTypes.stream()
                .filter(FieldType::isDBFieldType)
                .map(FieldType::getDbFieldType)
                .collect(Collectors.toList())
                .stream().filter(dbFieldType -> dbFieldType.isId() && dbFieldType.isGenerated())
                .findFirst()
                .orElseGet(() -> null);
    }

    private static<T> Constructor<T> lookupDefaultConstructor(Class<T> clazz) throws NoSuchMethodException {
        for (Constructor<?> constructor: clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return (Constructor<T>) constructor;
            }
        }
        throw new IllegalArgumentException("No define default constructor");
    }
}

