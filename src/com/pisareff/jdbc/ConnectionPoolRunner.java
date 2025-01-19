package com.pisareff.jdbc;

import com.pisareff.jdbc.util.ConnectionPool;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolRunner {
    public static void main(String[] args) throws SQLException {
        String sql = "SELECT id FROM ticket WHERE flight_id = 1";

        try (var connection = ConnectionPool.get();
        var statement = connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                System.out.println(resultSet.getLong("id"));
            }
        } finally {
            ConnectionPool.closeConnectionPool();
        }

    }
}
