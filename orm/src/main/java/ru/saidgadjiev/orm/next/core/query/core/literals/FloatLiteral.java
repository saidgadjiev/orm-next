package ru.saidgadjiev.orm.next.core.query.core.literals;

import ru.saidgadjiev.orm.next.core.query.visitor.QueryVisitor;

/**
 * Created by said on 03.02.2018.
 */
public class FloatLiteral implements Literal<Float> {

    private final float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    @Override
    public String getOriginal() {
        return String.valueOf(value);
    }

    @Override
    public Float get() {
        return value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        visitor.finish(this);
    }
}