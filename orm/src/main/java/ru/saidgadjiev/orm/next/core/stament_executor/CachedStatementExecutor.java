package ru.saidgadjiev.orm.next.core.stament_executor;

import ru.saidgadjiev.orm.next.core.cache.CacheContext;
import ru.saidgadjiev.orm.next.core.cache.ObjectCache;
import ru.saidgadjiev.orm.next.core.field.field_type.DBFieldType;
import ru.saidgadjiev.orm.next.core.field.field_type.IDBFieldType;
import ru.saidgadjiev.orm.next.core.stament_executor.result_mapper.ResultsMapper;
import ru.saidgadjiev.orm.next.core.table.TableInfo;
import ru.saidgadjiev.orm.next.core.table.TableInfoManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class CachedStatementExecutor implements IStatementExecutor {

    private IStatementExecutor delegate;

    private CacheContext cacheContext;

    public CachedStatementExecutor(CacheContext cacheContext,
                                   IStatementExecutor delegate) {
        this.cacheContext = cacheContext;
        this.delegate = delegate;
    }

    @Override
    public <T> int create(Connection connection, Collection<T> objects, Class<T> tClass) throws SQLException {
        delegate.create(connection, objects);

        if (cacheContext.isCaching(tClass) && cacheContext.getObjectCache().isPresent()) {
            ObjectCache objectCache = cacheContext.getObjectCache().get();

            if (tableInfo.getPrimaryKey().isPresent()) {
                for (T object : objects) {
                    IDBFieldType idbFieldType = tableInfo.getPrimaryKey().get();

                    try {
                        objectCache.put(tableInfo.getTableClass(), idbFieldType.access(object), object);
                    } catch (Exception ex) {
                        throw new SQLException(ex);
                    }
                }
            }
        }

        return 0;
    }

    @Override
    public <T> int create(Connection connection, T object) throws SQLException {
        Integer count = delegate.create(connection, object);

        if (count > 0 && cacheContext.isCaching(tClass) && cacheContext.getObjectCache().isPresent()) {
            ObjectCache objectCache = cacheContext.getObjectCache().get();

            if (tableInfo.getPrimaryKey().isPresent()) {
                IDBFieldType idbFieldType = tableInfo.getPrimaryKey().get();

                try {
                    objectCache.put(tableInfo.getTableClass(), idbFieldType.access(object), object);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }
            }
        }

        return count;
    }

    @Override
    public <T> boolean createTable(Connection connection, Class<T> tClass, boolean ifNotExists) throws SQLException {
        return delegate.createTable(connection, ifNotExists);
    }

    @Override
    public <T> boolean dropTable(Connection connection, Class<T> tClass, boolean ifExists) throws SQLException {
        return delegate.dropTable(connection, ifExists);
    }

    @Override
    public <T> int update(Connection connection, T object) throws SQLException {
        Integer count = delegate.update(connection, object);

        if (count > 0) {
            try {
                TableInfo<T> tableInfo = TableInfoManager.buildOrGet(object.getClass());

            IDBFieldType idFieldType = tableInfo.getPrimaryKey().get();

                if (cacheContext.isCaching(object.getClass()) && cacheContext.getObjectCache().isPresent()) {
                    ObjectCache objectCache = cacheContext.getObjectCache().get();
                    T cachedData = objectCache.get(tableInfo.getTableClass(), idFieldType.access(object));

                    if (cachedData != null) {
                        copy(object, cachedData);
                    }
                }
            } catch (Exception ex) {
                throw new SQLException(ex);
            }
        }

        return count;
    }

    @Override
    public <T> int delete(Connection connection, T object) throws SQLException {
        try {
            TableInfo<T> tableInfo = TableInfoManager.buildOrGet(object.getClass());

            IDBFieldType dbFieldType = tableInfo.getPrimaryKey().get();
            Integer result = delegate.delete(connection, object);
            Object id = dbFieldType.access(object);

            if (cacheContext.isCaching(object.getClass())) {
                cacheContext.getObjectCache().ifPresent(objectCache -> objectCache.invalidate(tableInfo.getTableClass(), id));
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public <T, ID> int deleteById(Connection connection, Class<T> tClass, ID id) throws SQLException {
        try {
            TableInfo<T> tableInfo = TableInfoManager.buildOrGet(tClass);

            Integer result = delegate.deleteById(connection, tClass, id);

            if (cacheContext.isCaching(tClass)) {
                cacheContext.getObjectCache().ifPresent(objectCache -> objectCache.invalidate(tableInfo.getTableClass(), id));
            }

            return result;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public <T, ID> T queryForId(Connection connection, Class<T> tClass, ID id) throws SQLException {
        try {
            TableInfo<T> tableInfo = TableInfoManager.buildOrGet(tClass);

            if (cacheContext.isCaching(tClass) && cacheContext.getObjectCache().isPresent()) {

                ObjectCache objectCache = cacheContext.getObjectCache().get();

                if (objectCache.contains(tableInfo.getTableClass(), id)) {
                    return objectCache.get(tableInfo.getTableClass(), id);
                }
            }
            T object = delegate.queryForId(connection, tClass, id);

            if (object != null && cacheContext.isCaching(tClass)) {
                cacheContext.getObjectCache().ifPresent(objectCache -> objectCache.put(tableInfo.getTableClass(), id, object));
            }

            return object;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public <T> List<T> queryForAll(Connection connection, Class<T> tClass) throws SQLException {
        List<T> result = delegate.queryForAll(connection, tClass);

        try {
            TableInfo<T> tableInfo = TableInfoManager.buildOrGet(tClass);

            if (tableInfo.getPrimaryKey().isPresent() && cacheContext.isCaching(tClass) && cacheContext.getObjectCache().isPresent()) {
                IDBFieldType idbFieldType = tableInfo.getPrimaryKey().get();
                ObjectCache objectCache = cacheContext.getObjectCache().get();

                for (T object : result) {
                    objectCache.put(tableInfo.getTableClass(), idbFieldType.access(object), object);
                }
            }
        } catch (Exception ex) {
            throw new SQLException(ex);
        }

        return result;
    }

    @Override
    public <T> void createIndexes(Connection connection, Class<T> tClass) throws SQLException {
        delegate.createIndexes(connection, tClass);
    }

    @Override
    public <T> void dropIndexes(Connection connection, Class<T> tClass) throws SQLException {
        delegate.dropIndexes(connection, tClass);
    }

    @Override
    public <R> GenericResults<R> query(Connection connection, String query, ResultsMapper<R> resultsMapper) throws SQLException {
        return delegate.query(connection, query, resultsMapper);
    }

    @Override
    public long queryForLong(String query, Connection connection) throws SQLException {
        return delegate.queryForLong(query, connection);
    }

    @Override
    public <T> long countOff(Connection connection, Class<T> tClass) throws SQLException {
        return delegate.countOff(connection, tClass);
    }

    private <T> void copy(T srcObject, T destObject) throws Exception {
        TableInfo<T> tableInfo = TableInfoManager.buildOrGet(srcObject.getClass());
        for (DBFieldType fieldType : tableInfo.toDBFieldTypes()) {
            fieldType.assign(destObject, fieldType.access(srcObject));
        }
    }
}
