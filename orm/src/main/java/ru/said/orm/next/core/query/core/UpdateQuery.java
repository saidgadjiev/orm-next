package ru.said.orm.next.core.query.core;

import ru.said.orm.next.core.field.field_type.DBFieldType;
import ru.said.orm.next.core.query.core.column_spec.ColumnSpec;
import ru.said.orm.next.core.query.core.common.UpdateValue;
import ru.said.orm.next.core.query.core.condition.Equals;
import ru.said.orm.next.core.query.core.condition.Expression;
import ru.said.orm.next.core.query.core.literals.Param;
import ru.said.orm.next.core.query.visitor.QueryElement;
import ru.said.orm.next.core.query.visitor.QueryVisitor;
import ru.said.orm.next.core.stament_executor.FieldConverter;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс UPDATE запроса
 */

public class UpdateQuery implements QueryElement {

    /**
     * Название типа
     */
    private final String typeName;

    /**
     * Вставляемые значения
     */
    private final List<UpdateValue> updateValues = new ArrayList<>();

    private Expression where = new Expression();

    private UpdateQuery(String typeName) {
        this.typeName = typeName;
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

    public Expression getWhere() {
        return where;
    }

    public void setWhere(Expression where) {
        this.where = where;
    }

    /**
     * Создание экземпляра UpdateQuery
     * @return возвращет экземляр UpdateQuery
     */
    public static<T, ID> UpdateQuery buildQuery(String typeName, List<DBFieldType> fieldTypes, String idColumnName, T object) throws SQLException {
        UpdateQuery updateQuery = new UpdateQuery(typeName);

        try {
            for (DBFieldType fieldType : fieldTypes) {
                updateQuery.updateValues.add(
                        new UpdateValue(
                                fieldType.getColumnName(), FieldConverter.getInstanse().convert(fieldType.getDataType(), fieldType.access(object)))
                );
            }
            AndCondition andCondition = new AndCondition();

            andCondition.add(new Equals(new ColumnSpec(idColumnName).alias(new Alias(typeName)), new Param()));
            updateQuery.getWhere().getConditions().add(andCondition);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new SQLException(ex);
        }

        return updateQuery;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.start(this)) {
            for (UpdateValue updateValue : updateValues) {
                updateValue.accept(visitor);
            }
        }
        where.accept(visitor);
        visitor.finish(this);
    }
}
