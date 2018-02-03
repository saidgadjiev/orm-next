package ru.saidgadjiev.orm.next.core.stament_executor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by said on 04.02.2018.
 */
public interface IStatement extends AutoCloseable {

    DatabaseResults executeQuery(String sql) throws SQLException;

    int executeUpdate(String sql) throws SQLException;

    ResultSet getGeneratedKeys() throws SQLException;
}