package test_table;

import field.*;
import table.Table;

/**
 * Created by said on 02.05.17.
 */
@Table(name = "foo2")
public class Foo2 {
    @TableField(id = true, autoGeneratedId = true, dataType = DataType.LONG, fieldName = "id")
    private long id;

    @TableField(dataType = DataType.STRING, fieldName = "test_name")
    private String name;

    @ManyToOne
    @TableField(canBeNull = false, dataType = DataType.INTEGER, fieldName = "foo_id")
    Foo foo;

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

    public void setFoo(Foo foo) {
        this.foo = foo;
    }

    public Foo getFoo() {
        return foo;
    }

    @Override
    public String toString() {
        return "test_table.Foo2{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Foo2 foo2 = (Foo2) o;

        if (id != foo2.id) return false;
        return name != null ? name.equals(foo2.name) : foo2.name == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
