package ru.saidgadjiev.ormnext.core.table.internal.persister;

import ru.saidgadjiev.ormnext.core.connection.DatabaseResults;
import ru.saidgadjiev.ormnext.core.dao.Session;
import ru.saidgadjiev.ormnext.core.dao.SessionManager;
import ru.saidgadjiev.ormnext.core.loader.ResultSetContext;
import ru.saidgadjiev.ormnext.core.loader.object.OrmNextMethodHandler;
import ru.saidgadjiev.ormnext.core.loader.rowreader.RowReader;
import ru.saidgadjiev.ormnext.core.loader.rowreader.RowReaderImpl;
import ru.saidgadjiev.ormnext.core.loader.rowreader.RowResult;
import ru.saidgadjiev.ormnext.core.loader.rowreader.entityinitializer.EntityInitializer;
import ru.saidgadjiev.ormnext.core.query.space.EntityQuerySpace;
import ru.saidgadjiev.ormnext.core.table.internal.alias.EntityAliasResolverContext;
import ru.saidgadjiev.ormnext.core.table.internal.alias.EntityAliases;
import ru.saidgadjiev.ormnext.core.table.internal.alias.UIDGenerator;
import ru.saidgadjiev.ormnext.core.table.internal.instatiator.Instantiator;
import ru.saidgadjiev.ormnext.core.table.internal.instatiator.ObjectInstantiator;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.DatabaseEntityMetadata;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;
import ru.saidgadjiev.ormnext.core.table.internal.visitor.DefaultEntityMetadataVisitor;
import ru.saidgadjiev.proxymaker.ProxyMaker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity persister. Holds entity aliases and aliases for entity references.
 *
 * @author Said Gadjiev
 */
public class DatabaseEntityPersister {

    /**
     * Alias context.
     *
     * @see EntityAliasResolverContext
     */
    private EntityAliasResolverContext aliasResolverContext = new EntityAliasResolverContext();

    /**
     * Entity meta data.
     */
    private DatabaseEntityMetadata<?> databaseEntityMetadata;

    /**
     * Entity object instantiator.
     */
    private Instantiator instantiator;

    /**
     * Current session manager.
     */
    private SessionManager sessionManager;

    /**
     * Entity results reader.
     *
     * @see RowReader
     */
    private RowReader rowReader;

    /**
     * Entity root initializer.
     *
     * @see EntityInitializer
     */
    private EntityInitializer rootEntityInitializer;

    /**
     * Unique uid generator. It use for associate aliases with entity initializer.
     */
    private UIDGenerator uidGenerator = new UIDGenerator();

    /**
     * Proxy factory. Use for lazy fetch {@link ru.saidgadjiev.ormnext.core.field.ForeignColumn}.
     *
     * @see ProxyMaker
     */
    private ProxyMaker proxyMaker = new ProxyMaker();

    /**
     * Entity query space.
     *
     * @see EntityQuerySpace
     */
    private EntityQuerySpace entityQuerySpace;

    /**
     * Create a new instance.
     *
     * @param databaseEntityMetadata entity metadata
     * @param sessionManager         session manager
     */
    public DatabaseEntityPersister(DatabaseEntityMetadata<?> databaseEntityMetadata, SessionManager sessionManager) {
        this.databaseEntityMetadata = databaseEntityMetadata;
        this.instantiator = new ObjectInstantiator(databaseEntityMetadata.getTableClass());
        this.sessionManager = sessionManager;

        initRootInitializer();
    }

    /**
     * Initialize entity initializer {@link EntityInitializer} for root entity.
     */
    private void initRootInitializer() {
        String nextUID = uidGenerator.nextUID();
        EntityAliases entityAliases = aliasResolverContext.resolveAliases(nextUID, databaseEntityMetadata);

        rootEntityInitializer = new EntityInitializer(nextUID, entityAliases, this);
    }

    /**
     * Load entity object list from database results.
     *
     * @param <T>             target result object type
     * @param session         target session
     * @param databaseResults target database results
     * @return object list
     * @throws SQLException any SQL exceptions
     */
    public <T> List<RowResult<T>> load(Session session, DatabaseResults databaseResults) throws SQLException {
        ResultSetContext resultSetContext = new ResultSetContext(
                session,
                databaseResults,
                sessionManager.cacheHelper()
        );
        List<RowResult<T>> results = new ArrayList<>();

        while (databaseResults.next()) {
            RowResult<T> rowResult = (RowResult<T>) rowReader.startRead(resultSetContext);

            if (rowResult.isNew()) {
                results.add(rowResult);
            }
        }
        rowReader.finishRead(resultSetContext);

        return results;
    }

    /**
     * Perssiter initialize method.
     *
     * @param metaModel target meta model
     * @see MetaModel
     */
    public void initialize(MetaModel metaModel) {
        DefaultEntityMetadataVisitor visitor = new DefaultEntityMetadataVisitor(
                databaseEntityMetadata,
                metaModel,
                aliasResolverContext,
                uidGenerator,
                rootEntityInitializer
        );

        databaseEntityMetadata.accept(visitor);
        rowReader = new RowReaderImpl(
                visitor.getEntityInitializers(),
                visitor.getCollectionInitializers(),
                rootEntityInitializer
        );
        entityQuerySpace = visitor.getEntityQuerySpace();
    }

    /**
     * Create new proxy object which will be use for lazy instantiation
     * {@link ru.saidgadjiev.ormnext.core.field.ForeignColumn}.
     *
     * @param entityClass     target entity class
     * @param key             target entity id
     * @param keyPropertyName target key property name
     * @return new proxy object
     * @throws SQLException any SQL exceptions
     */
    public Object createProxy(Class<?> entityClass, String keyPropertyName, Object key) throws SQLException {
        try {
            return proxyMaker
                    .superClass(entityClass)
                    .make(new OrmNextMethodHandler(sessionManager, entityClass, keyPropertyName, key));
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    /**
     * Return current entity metadata.
     *
     * @return current metadata
     */
    public DatabaseEntityMetadata<?> getMetadata() {
        return databaseEntityMetadata;
    }

    /**
     * Create new entity class object.
     *
     * @return new object
     */
    public Object instance() {
        return instantiator.instantiate();
    }

    /**
     * Return current entity query space.
     *
     * @return entity query space
     */
    public EntityQuerySpace getEntityQuerySpace() {
        return entityQuerySpace;
    }

    public EntityAliases getAliases() {
        return rootEntityInitializer.getEntityAliases();
    }
}
