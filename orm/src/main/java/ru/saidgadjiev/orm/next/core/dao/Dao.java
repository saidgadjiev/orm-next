package ru.saidgadjiev.orm.next.core.dao;

import ru.saidgadjiev.orm.next.core.cache.ObjectCache;
import ru.saidgadjiev.orm.next.core.support.ConnectionSource;

import java.sql.SQLException;

/**
 * Класс для DAO
 * @param <T> тип объекта
 * @param <ID> тип id
 */
public interface Dao extends BaseDao {

    void caching(boolean flag, Class<?> ... classes);

    void setObjectCache(ObjectCache objectCache, Class<?> ... classes);

    ConnectionSource getDataSource();

    TransactionImpl transaction() throws SQLException;
}
