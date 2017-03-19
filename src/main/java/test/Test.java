package test;

import field.DBField;
import field.DataType;
import field.OneToOne;
import table.DBTable;

/**
 * Created by said on 25.02.17.
 */
@DBTable(name = "test")
public class Test {

    @DBField(id = true, autoGeneratedId = true, dataType = DataType.LONG, fieldName = "id")
    private long id;

    @DBField(dataType = DataType.STRING, fieldName = "test_name")
    private String name;

    @OneToOne
    @DBField(canBeNull = false, dataType = DataType.INTEGER, fieldName = "test1_id")
    private Test1 test1;

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
        return "Test{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", test1=" + test1 +
                '}';
    }

    public Test1 getTest1() {
        return test1;
    }

    public void setTest1(Test1 test1) {
        this.test1 = test1;
    }
}
