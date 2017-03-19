package test;

import field.DBField;
import field.DataType;
import table.DBTable;

/**
 * Created by said on 18.03.17.
 */

@DBTable(name = "test1")
public class Test1 {

    @DBField(id = true, autoGeneratedId = true, dataType = DataType.LONG, fieldName = "id")
    private long id;

    @DBField(dataType = DataType.STRING, fieldName = "test_name")
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Test1{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
