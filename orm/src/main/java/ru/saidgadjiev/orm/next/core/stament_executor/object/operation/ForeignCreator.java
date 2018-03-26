package ru.saidgadjiev.orm.next.core.stament_executor.object.operation;

import ru.saidgadjiev.orm.next.core.dao.BaseSessionManagerImpl;
import ru.saidgadjiev.orm.next.core.dao.Session;
import ru.saidgadjiev.orm.next.core.field.field_type.ForeignFieldType;
import ru.saidgadjiev.orm.next.core.support.ConnectionSource;
import ru.saidgadjiev.orm.next.core.table.TableInfo;
import ru.saidgadjiev.orm.next.core.table.TableInfoManager;

/**
 * Created by said on 10.02.2018.
 */
public class ForeignCreator<O> implements IObjectOperation<Void, O> {

    private ConnectionSource source;

    public ForeignCreator(ConnectionSource source) {
        this.source = source;
    }

    @Override
    public Void execute(O object) throws Exception {
        TableInfo<O> tableInfo = TableInfoManager.buildOrGet((Class<O>) object.getClass());

        for (ForeignFieldType fieldType : tableInfo.toForeignFieldTypes()) {
            Object foreignObject = fieldType.access(object);

            if (foreignObject != null && fieldType.isForeignAutoCreate()) {
                Session foreignDao = new BaseSessionManagerImpl(source).getCurrentSession();

                foreignDao.create(foreignObject);
            }
        }

        return null;
    }
}
