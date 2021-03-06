package ru.saidgadjiev.ormnext.core.query.visitor.element.function;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;
import ru.saidgadjiev.ormnext.core.query.visitor.element.condition.Expression;

/**
 * This class represent sql AVG.
 *
 * @author Said Gadjiev
 */
public class AVG implements Function {

    /**
     * AVG expression.
     * @see Expression
     */
    private final Expression expression;

    /**
     * Create a new instance.
     * @param expression target expression
     */
    public AVG(Expression expression) {
        this.expression = expression;
    }

    /**
     * Return current expression.
     * @return expression
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            expression.accept(visitor);
        }
    }
}
