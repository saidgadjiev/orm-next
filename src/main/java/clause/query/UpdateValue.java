package clause.query;

import clause.element.Value;
import dao.visitor.QueryElement;
import dao.visitor.QueryVisitor;

/**
 * Created by said on 17.06.17.
 */
public class UpdateValue implements QueryElement {
    private String name;
    private Value value;

    public UpdateValue(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
    }
}
