package ru.said.orm.next.core.dao;

import ru.said.orm.next.core.cache.ObjectCache;
import ru.said.orm.next.core.stament_executor.GenericResults;
import ru.said.orm.next.core.stament_executor.IStatementExecutor;
import ru.said.orm.next.core.stament_executor.ResultsMapper;
import ru.said.orm.next.core.support.ConnectionSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class TransactionManager<T, ID> implements Transaction<T, ID> {

    private final IStatementExecutor<T, ID> statementExecutor;

    private final Connection connection;

    private Callable<Void> close;

    public TransactionManager(IStatementExecutor<T, ID> statementExecutor, Connection connection, Callable<Void> close) {
        this.statementExecutor = statementExecutor;
        this.connection = connection;
        this.close = close;
    }

    @Override
    public int create(T object) throws SQLException {
        check();
        return statementExecutor.create(connection, object);
    }

    @Override
    public boolean createTable(boolean ifNotExists) throws SQLException {
        check();
        return statementExecutor.createTable(connection, ifNotExists);
    }

    @Override
    public T queryForId(ID id) throws SQLException {
        check();
        return statementExecutor.queryForId(connection, id);
    }

    @Override
    public List<T> queryForAll() throws SQLException {
        check();
        return statementExecutor.queryForAll(connection);
    }

    @Override
    public int update(T object) throws SQLException {
        check();
        return statementExecutor.update(connection, object);
    }

    @Override
    public int delete(T object) throws SQLException {
        check();
        return statementExecutor.delete(connection, object);
    }

    @Override
    public int deleteById(ID id) throws SQLException {
        check();
        return statementExecutor.deleteById(connection, id);
    }

    @Override
    public void caching(boolean flag, ObjectCache objectCache) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public boolean dropTable(boolean ifExists) throws SQLException {
        check();
        return statementExecutor.dropTable(connection, ifExists);
    }

    @Override
    public void createIndexes() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void dropIndexes() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public <R> GenericResults<R> query(String query, ResultsMapper<R> resultsMapper) throws SQLException {
        check();
        return statementExecutor.query(connection, query, resultsMapper);
    }

    @Override
    public ConnectionSource getDataSource() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public TransactionManager<T, ID> transaction() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void beginTrans() throws SQLException {
        connection.setAutoCommit(false);
    }

    @Override
    public void commitTrans() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    private void check() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is invalid");
        }
    }

    @Override
    public void close() throws Exception{
        close.call();
    }
}
