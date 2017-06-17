package clause.query;

import dao.visitor.QueryElement;
import dao.visitor.QueryVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 14.06.17.
 */
public class CreateQuery implements QueryElement {

    private List<UpdateValue> values = new ArrayList<>();
    private String name;

    public CreateQuery(String name) {
        this.name = name;
    }

    public void addUpdateValue(UpdateValue updateValue) {
        values.add(updateValue);
    }

    public List<UpdateValue> getValues() {
        return values;
    }

    public String getTableName() {
        return name;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        visitor.finish(this);
    }
}
