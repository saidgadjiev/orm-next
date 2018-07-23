package ru.saidgadjiev.ormnext.core.dao;

import ru.saidgadjiev.ormnext.core.cache.Cache;
import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;
import ru.saidgadjiev.ormnext.core.connection.DatabaseConnection;
import ru.saidgadjiev.ormnext.core.connection.source.ConnectionSource;
import ru.saidgadjiev.ormnext.core.loader.EntityLoader;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;

import java.sql.SQLException;
import java.util.Map;

/**
 * Implementation of {@link SessionManager}.
 *
 * @author Said Gadjiev
 */
public class SessionManagerImpl implements CacheSessionManager {

    /**
     * Connection source. Use for obtain new database connection.
     */
    private final ConnectionSource<?> dataSource;

    /**
     * Registered loaders.
     */
    private final Map<EntityLoader.Loader, EntityLoader> registeredLoaders;

    /**
     * Cache part.
     */
    private Cache cache;

    /**
     * Meta model.
     *
     * @see MetaModel
     */
    private MetaModel metaModel;

    /**
     * Database engine.
     *
     * @see DatabaseEngine
     */
    private DatabaseEngine<?> databaseEngine;

    /**
     * Create new instance from requested options. It can be create only from {@link SessionManagerBuilder}.
     *
     * @param registeredLoaders target registered loaders
     * @param connectionSource  target connection source
     * @param metaModel         target meta model
     * @param databaseEngine    target database engine
     */
    SessionManagerImpl(ConnectionSource<?> connectionSource,
                       Map<EntityLoader.Loader, EntityLoader> registeredLoaders,
                       MetaModel metaModel,
                       DatabaseEngine<?> databaseEngine) throws SQLException {
        this.dataSource = connectionSource;
        this.metaModel = metaModel;
        this.databaseEngine = databaseEngine;
        this.registeredLoaders = registeredLoaders;

        this.metaModel.init(this);
    }

    @Override
    public Session createSession() throws SQLException {
        DatabaseConnection<?> databaseConnection = dataSource.getConnection();

        if (cache != null) {
            return new CacheSession(
                    cache,
                    new SessionImpl(dataSource, registeredLoaders, databaseConnection, this)
            );
        } else {
            return new SessionImpl(dataSource, registeredLoaders, databaseConnection, this);
        }
    }

    @Override
    public MetaModel getMetaModel() {
        return metaModel;
    }

    @Override
    public void setObjectCache(Class<?> entityType, ObjectCache objectCache) {
        if (cache != null) {
            cache.setCache(entityType, objectCache);
        }
    }

    @Override
    public void setObjectCache(Class<?>[] entityClass, ObjectCache objectCache) {
        for (Class<?> entityType : entityClass) {
            setObjectCache(entityType, objectCache);
        }
    }

    @Override
    public void enableDefaultCache() {
        if (cache != null) {
            cache.enableDefaultCache();
        }
    }

    @Override
    public <T> DatabaseEngine<T> getDatabaseEngine() {
        return (DatabaseEngine<T>) databaseEngine;
    }

    @Override
    public void upgrade(Cache cache) {
        this.cache = cache;

        cache.init(metaModel, databaseEngine);
    }

    @Override
    public CacheEvict getCacheEvictApi() {
        return cache == null ? null : cache.evictApi();
    }

    @Override
    public void close() throws SQLException {
        if (cache != null) {
            cache.close();
        }
        dataSource.close();
    }

    @Override
    public void putToCache(Object id, Object data) {
        if (cache != null) {
            cache.putToCache(id, data);
        }
    }
}
