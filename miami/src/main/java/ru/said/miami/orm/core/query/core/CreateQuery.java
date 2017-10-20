package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.field.DBFieldType;
import ru.said.miami.orm.core.query.visitor.DefaultVisitor;
import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс INSERT запроса
 */
public class CreateQuery implements Query, QueryElement {

    /**
     * Название типа
     */
    private final String typeName;

    /**
     * Вставляемые значения
     */
    private final List<UpdateValue> updateValues = new ArrayList<>();

    private QueryVisitor visitor;

    private Number generatedKey;

    private CreateQuery(String typeName, QueryVisitor visitor) {
        this.typeName = typeName;
        this.visitor = visitor;
    }

    /**
     * Добавление нового значения
     *
     * @param updateValue добавляемое значение
     */
    public void add(UpdateValue updateValue) {
        updateValues.add(updateValue);
    }

    /**
     * Добавление коллекции значений
     *
     * @param values
     */
    public void addAll(List<UpdateValue> values) {
        updateValues.addAll(values);
    }

    /**
     * Получение списка значений
     *
     * @return
     */
    public List<UpdateValue> getUpdateValues() {
        return updateValues;
    }

    /**
     * Получение имени типа
     *
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Integer execute(Connection connection) throws SQLException {
        this.accept(visitor);
        String sql = visitor.getQuery();

        try (Statement statement = connection.createStatement()) {
            Integer count = statement.executeUpdate(sql);
            ResultSet rsKeys = statement.getGeneratedKeys();

            if (rsKeys.next()) {
                generatedKey = getIdColumnData(rsKeys, rsKeys.getMetaData(), 1);
            } else {
                generatedKey = null;
            }

            return count;
        }
    }

    private Number getIdColumnData(ResultSet resultSet, ResultSetMetaData metaData, int columnIndex) throws SQLException {
        int typeVal = metaData.getColumnType(columnIndex);

        switch (typeVal) {
            case Types.BIGINT :
            case Types.DECIMAL :
            case Types.NUMERIC :
                return resultSet.getLong(columnIndex);
            case Types.INTEGER :
                return resultSet.getInt(columnIndex);
            default :
                throw new SQLException("Unknown DataType for typeVal " + typeVal + " in column " + columnIndex);
        }
    }

    public Optional<Number> getGeneratedKey() {
        return Optional.ofNullable(generatedKey);
    }

    public static<T> CreateQuery buildQuery(String typeName, List<DBFieldType> fieldTypes, T object) throws SQLException {
        CreateQuery createQuery = new CreateQuery(typeName, new DefaultVisitor());

        try {
            if (fieldTypes != null && object != null) {
                for (DBFieldType fieldType : fieldTypes) {
                    createQuery.updateValues.add(
                            new UpdateValue(
                                    fieldType.getColumnName(), FieldConverter.getInstanse().convert(fieldType.getDataType(), fieldType.access(object)))
                    );
                }
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new SQLException(ex);
        }

        return createQuery;
    }


    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.start(this)) {
            for (UpdateValue updateValue : updateValues) {
                updateValue.accept(visitor);
            }
        }
        visitor.finish(this);
    }
}