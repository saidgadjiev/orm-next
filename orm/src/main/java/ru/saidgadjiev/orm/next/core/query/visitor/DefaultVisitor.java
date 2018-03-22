package ru.saidgadjiev.orm.next.core.query.visitor;

import ru.saidgadjiev.orm.next.core.db.DatabaseType;
import ru.saidgadjiev.orm.next.core.field.DataType;
import ru.saidgadjiev.orm.next.core.query.core.*;
import ru.saidgadjiev.orm.next.core.query.core.clause.*;
import ru.saidgadjiev.orm.next.core.query.core.clause.from.FromJoinedTables;
import ru.saidgadjiev.orm.next.core.query.core.clause.from.FromTable;
import ru.saidgadjiev.orm.next.core.query.core.clause.select.SelectAll;
import ru.saidgadjiev.orm.next.core.query.core.clause.select.SelectColumnsList;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.ColumnSpec;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.DisplayedColumn;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.DisplayedColumnSpec;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.DisplayedOperand;
import ru.saidgadjiev.orm.next.core.query.core.common.TableRef;
import ru.saidgadjiev.orm.next.core.query.core.common.UpdateValue;
import ru.saidgadjiev.orm.next.core.query.core.condition.*;
import ru.saidgadjiev.orm.next.core.query.core.constraints.attribute.*;
import ru.saidgadjiev.orm.next.core.query.core.constraints.table.UniqueConstraint;
import ru.saidgadjiev.orm.next.core.query.core.function.AVG;
import ru.saidgadjiev.orm.next.core.query.core.function.CountAll;
import ru.saidgadjiev.orm.next.core.query.core.function.CountExpression;
import ru.saidgadjiev.orm.next.core.query.core.function.SUM;
import ru.saidgadjiev.orm.next.core.query.core.join.JoinExpression;
import ru.saidgadjiev.orm.next.core.query.core.join.JoinInfo;
import ru.saidgadjiev.orm.next.core.query.core.join.LeftJoin;
import ru.saidgadjiev.orm.next.core.query.core.literals.*;

import java.util.Iterator;

/**
 * Visitor по умолчанию
 */
public class DefaultVisitor extends NoActionVisitor {

    protected StringBuilder sql = new StringBuilder();

    private DatabaseType databaseType;

    private final String escapeEntity;

    private final String escapeLiteral;

    public DefaultVisitor(DatabaseType databaseType) {
        this.databaseType = databaseType;
        escapeEntity = databaseType.getEntityNameEscape();
        escapeLiteral = databaseType.getValueEscape();
    }

    public String getQuery() {
        return sql.toString();
    }

    @Override
    public void visit(CreateQuery tCreateQuery) {
        sql.append("INSERT INTO ").append(escapeEntity).append(tCreateQuery.getTypeName()).append(escapeEntity);

        if (tCreateQuery.getUpdateValues().isEmpty()) {
            sql.append(" ").append(databaseType.appendNoColumn());

            return;
        } else {
            sql.append(" (");
        }

        for (Iterator<UpdateValue> iterator = tCreateQuery.getUpdateValues().iterator(); iterator.hasNext(); ) {
            UpdateValue updateValue = iterator.next();

            sql.append(escapeEntity).append(updateValue.getName()).append(escapeEntity);
            if (iterator.hasNext()) {
                sql.append(",");
            }
        }
        sql.append(")");
        sql.append(" VALUES (");
        for (Iterator<UpdateValue> iterator = tCreateQuery.getUpdateValues().iterator(); iterator.hasNext(); ) {
            UpdateValue updateValue = iterator.next();
            RValue value = updateValue.getValue();

            value.accept(this);
            if (iterator.hasNext()) {
                sql.append(",");
            }
        }
        sql.append(")");
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (stringLiteral.getOriginal() == null) {
            sql.append("null");
        } else {
            sql.append(escapeLiteral).append(stringLiteral.getOriginal()).append(escapeLiteral);
        }
    }

    @Override
    public void visit(Select tSelectQuery) {
        sql.append("SELECT ");
        if (tSelectQuery.getSelectColumnsStrategy() != null) {
            tSelectQuery.getSelectColumnsStrategy().accept(this);
        }
        sql.append(" FROM ");
        if (tSelectQuery.getFrom() != null) {
            tSelectQuery.getFrom().accept(this);
        }
        if (!tSelectQuery.getWhere().getConditions().isEmpty()) {
            sql.append(" WHERE ");
        }
    }

    @Override
    public void visit(Expression expression) {
        for (Iterator<AndCondition> iterator = expression.getConditions().iterator(); iterator.hasNext(); ) {
            AndCondition andCondition = iterator.next();

            sql.append("(");

            for (Iterator<Condition> iterator1 = andCondition.getConditions().iterator(); iterator1.hasNext(); ) {
                Condition condition = iterator1.next();

                sql.append("(");

                condition.accept(this);

                sql.append(")");

                if (iterator1.hasNext()) {
                    sql.append(" AND ");
                }
            }

            sql.append(")");

            if (iterator.hasNext()) {
                sql.append(" OR ");
            }
        }
    }

    @Override
    public void visit(Equals equals) {
        equals.getFirst().accept(this);
        sql.append(" = ");
        equals.getSecond().accept(this);
    }

    @Override
    public void visit(ColumnSpec columnSpec) {
        if (columnSpec.getAlias() != null) {
            columnSpec.getAlias().accept(this);
            sql.append(".");
        }
        sql.append(escapeEntity).append(columnSpec.getName()).append(escapeEntity);
    }

    @Override
    public void visit(TableRef tableRef) {
        sql.append(escapeEntity).append(tableRef.getTableName()).append(escapeEntity);

        if (tableRef.getAlias() != null) {
            sql.append(" AS ");
            tableRef.getAlias().accept(this);
        }
    }

    @Override
    public void visit(CreateTableQuery tCreateTableQuery) {
        sql.append("CREATE TABLE ");
        if (tCreateTableQuery.isIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(escapeEntity).append(tCreateTableQuery.getTypeName()).append("` (");
        for (Iterator<AttributeDefinition> iterator = tCreateTableQuery.getAttributeDefinitions().iterator(); iterator.hasNext(); ) {
            AttributeDefinition attributeDefinition = iterator.next();

            sql.append(escapeEntity).append(attributeDefinition.getName()).append("` ");
            appendAttributeDataType(attributeDefinition);
            sql.append(" ");
            for (Iterator<AttributeConstraint> constraintIterator = attributeDefinition.getAttributeConstraints().iterator(); constraintIterator.hasNext(); ) {
                AttributeConstraint constraint = constraintIterator.next();

                constraint.accept(this);

                if (constraintIterator.hasNext()) {
                    sql.append(" ");
                }
            }
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        sql.append(")");
    }

    private void appendAttributeDataType(AttributeDefinition def) {
        DataType dataType = def.getDataType();

        switch (dataType) {
            case STRING:
            case DATE:
                sql.append("VARCHAR").append("(").append(def.getLength()).append(")");
                break;
            case INTEGER:
            case LONG:
                sql.append("INTEGER");
                break;
            case BOOLEAN:
                sql.append("BOOLEAN");
                break;
            case FLOAT:
                sql.append("FLOAT");
                break;
            case DOUBLE:
                sql.append("DOUBLE");
                break;
            case UNKNOWN:
                break;
        }


    }

    @Override
    public void visit(DeleteQuery deleteQuery) {
        sql.append("DELETE FROM ").append(deleteQuery.getTypeName());
        if (!deleteQuery.getWhere().getConditions().isEmpty()) {
            sql.append(" WHERE ");
        }
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        sql.append(intLiteral.getOriginal());
    }

    @Override
    public void visit(UpdateQuery updateQuery) {
        sql.append("UPDATE ").append(updateQuery.getTypeName()).append(" SET ");

        for (Iterator<UpdateValue> iterator = updateQuery.getUpdateValues().iterator(); iterator.hasNext(); ) {
            UpdateValue updateValue = iterator.next();
            RValue value = updateValue.getValue();

            sql.append(updateValue.getName()).append("=");
            value.accept(this);
            if (iterator.hasNext()) {
                sql.append(",");
            }
        }
        if (!updateQuery.getWhere().getConditions().isEmpty()) {
            sql.append(" WHERE ");
            updateQuery.getWhere().accept(this);
        }
    }

    @Override
    public void visit(DropTableQuery dropTableQuery) {
        sql.append("DROP TABLE ");
        if (dropTableQuery.isIfExists()) {
            sql.append("IF EXISTS ");
        }
        sql.append(escapeEntity).append(dropTableQuery.getTableName()).append(escapeEntity);
    }

    @Override
    public void visit(PrimaryKeyConstraint primaryKeyConstraint) {
        sql.append(databaseType.appendPrimaryKey(primaryKeyConstraint.isGenerated()));
    }

    @Override
    public void visit(UniqueConstraint uniqueConstraint) {
        sql.append(", UNIQUE (`");
        for (Iterator<String> iterator = uniqueConstraint.getUniqueColemns().iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next());
            if (iterator.hasNext()) {
                sql.append("`,`");
            }
        }
        sql.append("`)");
    }

    @Override
    public void visit(NotNullConstraint notNullConstraint) {
        sql.append("NOT NULL");
    }

    @Override
    public void visit(ReferencesConstraint referencesConstraint) {
        sql.append(" REFERENCES `")
                .append(referencesConstraint.getTypeName())
                .append("`(`")
                .append(referencesConstraint.getColumnName())
                .append("`)");
    }

    @Override
    public void visit(CreateIndexQuery createIndexQuery) {
        sql.append("CREATE ")
                .append(createIndexQuery.isUnique() ? "UNIQUE" : "INDEX")
                .append(" `")
                .append(createIndexQuery.getIndexName())
                .append("` ON `")
                .append(createIndexQuery.getTableName())
                .append("`(`");
        for (Iterator<String> iterator = createIndexQuery.getColumns().iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next());

            if (iterator.hasNext()) {
                sql.append("`,`");
            }
        }
        sql.append("`)");
    }

    @Override
    public void visit(DropIndexQuery dropIndexQuery) {
        sql.append("DROP INDEX `").append(dropIndexQuery.getName()).append(escapeEntity);
    }

    @Override
    public void visit(Param param) {
        sql.append("?");
    }

    @Override
    public void visit(SelectAll selectAll) {
        sql.append("*");
    }

    @Override
    public void visit(SelectColumnsList selectColumnsList) {
        for (Iterator<DisplayedColumnSpec> columnIterator = selectColumnsList.getColumns().iterator(); columnIterator.hasNext(); ) {
            DisplayedColumnSpec columnSpec = columnIterator.next();

            columnSpec.accept(this);
            if (columnIterator.hasNext()) {
                sql.append(", ");
            }
        }
    }

    @Override
    public void visit(Having having) {
        sql.append(" HAVING ");
        if (having.getExpression() != null) {
            having.getExpression().accept(this);
        }
    }

    @Override
    public void visit(GroupBy groupBy) {
        sql.append(" GROUP BY ");

        for (Iterator<GroupByItem> columnSpecIterator = groupBy.getGroupByItems().iterator(); columnSpecIterator.hasNext(); ) {
            GroupByItem columnSpec = columnSpecIterator.next();

            columnSpec.accept(this);
            if (columnSpecIterator.hasNext()) {
                sql.append(",");
            }
        }
    }

    @Override
    public void visit(FromTable fromTable) {
        fromTable.getTableRef().accept(this);
    }

    @Override
    public void visit(LeftJoin leftJoin) {
        sql.append(" LEFT JOIN ");
        leftJoin.getJoinedTableRef().accept(this);
        sql.append(" ON ");
        leftJoin.getExpression().accept(this);
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        sql.append(booleanLiteral.getOriginal());
    }

    @Override
    public void visit(JoinInfo joinInfo) {
        joinInfo.getTableRef().accept(this);
        sql.append(" ON ");
    }

    @Override
    public void visit(CountAll countAll) {
        sql.append("COUNT(*)");
    }

    @Override
    public void visit(FromJoinedTables fromJoinedTables) {
        fromJoinedTables.getTableRef().accept(this);
        for (JoinExpression joinExpression : fromJoinedTables.getJoinExpression()) {
            joinExpression.accept(this);
        }
    }

    @Override
    public void visit(DisplayedColumn displayedColumn) {
        displayedColumn.getColumnSpec().accept(this);
        if (displayedColumn.getAlias() != null) {
            sql.append(" AS ");
            displayedColumn.getAlias().accept(this);
        }
    }

    @Override
    public void visit(AVG avg) {

    }

    @Override
    public void visit(CountExpression countExpression) {
        sql.append("COUNT(");
        if (countExpression.getExpression() != null) {
            countExpression.getExpression().accept(this);
        }

        sql.append(")");
    }

    @Override
    public void visit(InSelect inSelect) {
        inSelect.getOperand().accept(this);
        sql.append(" IN (");
        inSelect.getSelect().accept(this);
        sql.append(")");
    }

    @Override
    public void visit(SUM sum) {
        sql.append("SUM(");
        if (sum.getExpression() != null) {
            sum.getExpression().accept(this);
        }
        sql.append(")");
    }

    @Override
    public void visit(OperandCondition operandCondition) {
        operandCondition.getOperand().accept(this);
    }

    @Override
    public void visit(Alias alias) {
        sql.append(escapeEntity).append(alias.getAlias()).append(escapeEntity);
    }

    @Override
    public void visit(DateLiteral dateLiteral) {
        sql.append(dateLiteral.getOriginal());
    }

    @Override
    public void visit(Default aDefault) {
        sql.append("DEFAULT ");
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        sql.append(floatLiteral.getOriginal());

    }

    @Override
    public void visit(DoubleLiteral doubleLiteral) {
        sql.append(doubleLiteral.getOriginal());
    }

    @Override
    public void visit(OrderBy orderBy) {
        sql.append(" ORDER BY ");
    }

    @Override
    public void visit(OrderByItem orderByItem, QueryVisitor visitor) {
        for (Iterator<ColumnSpec> iterator = orderByItem.getColumns().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(this);

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        if (!orderByItem.getColumns().isEmpty()) {
            sql.append(" ASC ");
        }
    }

    @Override
    public void visit(Limit limit) {
        sql.append(" LIMIT ").append(limit.getLimit());
    }

    @Override
    public void visit(Offset offset) {
        sql.append(" OFFSET ").append(offset.getOffset());
    }

    @Override
    public void visit(DisplayedOperand displayedOperand) {
        displayedOperand.getOperand().accept(this);
        if (displayedOperand.getAlias() != null) {
            sql.append(" AS ");
            displayedOperand.getAlias().accept(this);
        }
    }
}
