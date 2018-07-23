package ru.saidgadjiev.ormnext.core.loader.rowreader;

import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignCollectionColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.field.fieldtype.SimpleDatabaseColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.loader.ResultSetContext;
import ru.saidgadjiev.ormnext.core.table.internal.alias.EntityAliases;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.DatabaseEntityMetadata;
import ru.saidgadjiev.ormnext.core.table.internal.persister.DatabaseEntityPersister;
import ru.saidgadjiev.ormnext.core.table.internal.visitor.EntityMetadataVisitor;

import java.sql.SQLException;

/**
 * Created by said on 23.07.2018.
 */
public class CreateProcessingState implements EntityMetadataVisitor {

    private ResultSetContext resultSetContext;

    private String uid;

    private DatabaseEntityPersister persister;

    private EntityAliases aliases;

    public CreateProcessingState(ResultSetContext resultSetContext,
                                 DatabaseEntityPersister persister,
                                 String uid,
                                 EntityAliases aliases) {
        this.resultSetContext = resultSetContext;
        this.persister = persister;
        this.uid = uid;
        this.aliases = aliases;
    }

    @Override
    public boolean start(DatabaseEntityMetadata<?> databaseEntityMetadata) throws SQLException {
        return true;
    }

    @Override
    public boolean start(ForeignColumnTypeImpl foreignColumnType) throws SQLException {
        return false;
    }

    @Override
    public boolean start(ForeignCollectionColumnTypeImpl foreignCollectionColumnType) throws SQLException {
        return false;
    }

    @Override
    public void finish(ForeignColumnTypeImpl foreignColumnType) {

    }

    @Override
    public void finish(ForeignCollectionColumnTypeImpl foreignCollectionColumnType) {

    }

    @Override
    public boolean start(SimpleDatabaseColumnTypeImpl databaseColumnType) throws SQLException {
        if (databaseColumnType.id()) {
            String idAlias = aliases.getAliasByColumnName(databaseColumnType.columnName());

            if (!resultSetContext.isResultColumn(idAlias)) {
                return false;
            }

            ResultSetValue idValue = resultSetContext.getCurrentRow().get(idAlias);

            if (idValue.wasNull()) {
                return false;
            }
            Object id = idValue.getValue();
            ResultSetContext.EntityProcessingState processingState = resultSetContext.getOrCreateProcessingState(uid, id);
            Object entityInstance;

            if (processingState.getEntityInstance() == null) {
                entityInstance = persister.instance();

                processingState.setNew(true);
                processingState.setEntityInstance(entityInstance);
                databaseColumnType.assign(entityInstance, id);
            } else {
                processingState.setNew(false);

                entityInstance = processingState.getEntityInstance();
            }
            if (resultSetContext.getEntry(entityInstance.getClass(), id) == null) {
                resultSetContext.addEntry(id, entityInstance);
                resultSetContext.putToCache(id, entityInstance);
            }
            processingState.setValuesFromResultSet(resultSetContext.getCurrentRow());
        }
        return false;
    }

    @Override
    public void finish(SimpleDatabaseColumnTypeImpl databaseColumnType) {

    }
}
