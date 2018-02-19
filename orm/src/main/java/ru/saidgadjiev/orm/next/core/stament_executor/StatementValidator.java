package ru.saidgadjiev.orm.next.core.stament_executor;

import ru.saidgadjiev.orm.next.core.stament_executor.result_mapper.ResultsMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class StatementValidator implements IStatementExecutor<T, ID> {

    private IStatementExecutor<T, ID> delegate;

    public StatementValidator(IStatementExecutor<T, ID> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int create(Connection connection, Collection<T> objects) throws SQLException {
        return delegate.create(connection, objects);
    }

    @Override
    public int create(Connection connection, T object) throws SQLException {
        return delegate.create(connection, object);
    }

    @Override
    public boolean createTable(Connection connection, boolean ifNotExists) throws SQLException {
        return delegate.createTable(connection, ifNotExists);
    }

    @Override
    public boolean dropTable(Connection connection, boolean ifExists) throws SQLException {
        return delegate.dropTable(connection, ifExists);
    }

    @Override
    public int update(Connection connection, T object) throws SQLException {
        if (!tableInfo.getPrimaryKey().isPresent()) {
            throw new SQLException("Id is not defined");
        }

        return delegate.update(connection, object);
    }

    @Override
    public int delete(Connection connection, T object) throws SQLException {
        if (!tableInfo.getPrimaryKey().isPresent()) {
            throw new SQLException("Id is not defined");
        }

        return delegate.delete(connection, object);
    }

    @Override
    public int deleteById(Connection connection, ID id) throws SQLException {
        if (!tableInfo.getPrimaryKey().isPresent()) {
            throw new SQLException("Id is not defined");
        }

        return delegate.deleteById(connection, id);
    }

    @Override
    public T queryForId(Connection connection, ID id) throws SQLException {
        if (!tableInfo.getPrimaryKey().isPresent()) {
            throw new SQLException("Id is not defined");
        }

        return delegate.queryForId(connection, id);
    }

    @Override
    public List<T> queryForAll(Connection connection) throws SQLException {
        return delegate.queryForAll(connection);
    }

    @Override
    public void createIndexes(Connection connection) throws SQLException {
        delegate.createIndexes(connection);
    }

    @Override
    public void dropIndexes(Connection connection) throws SQLException {
        delegate.dropIndexes(connection);
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
    public long countOff(Connection connection) throws SQLException {
        return delegate.countOff(connection);
    }
}
