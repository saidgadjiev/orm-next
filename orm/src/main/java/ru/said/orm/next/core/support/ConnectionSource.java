package ru.said.orm.next.core.support;

import ru.said.orm.next.core.db.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionSource {

    Connection getConnection() throws SQLException;

    default void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }

    DatabaseType getDatabaseType();
}
