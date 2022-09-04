package ru.gb.javafxchat.server;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;

public class SQLiteDatabaseConnector implements Closeable {

    public static final String DB_PATH = "src/main/resources/ru/gb/javafxchat/server/db/database.db";

    private static Connection connection;

    public SQLiteDatabaseConnector() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к базе данных: " + e.getMessage(), e);
        }
    }

    protected Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

}
