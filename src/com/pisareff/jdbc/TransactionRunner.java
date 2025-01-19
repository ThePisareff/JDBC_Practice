package com.pisareff.jdbc;

import com.pisareff.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionRunner {
    public static void main(String[] args) throws SQLException {


    }

    private static void demoBatch() throws SQLException {

        var deleteFlightSql = "DELETE FROM flight where id =  8";
        var deleteTicketsSql = "DELETE FROM  ticket WHERE flight_id = 8";

        Connection connection = null;
        Statement statement = null;

        try {
            connection = ConnectionManager.open();
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.addBatch(deleteTicketsSql);
            statement.addBatch(deleteFlightSql);


            int[] results = statement.executeBatch();

            connection.commit();

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null){
                connection.close();
            }
            if (statement != null){
                statement.close();
            }
        }
    }

    private static void demoTransaction() throws SQLException {

        var deleteFlightSql = "DELETE FROM flight where id = ? ";
        var deleteTicketsSql = "DELETE FROM  ticket WHERE flight_id = ?";

        Connection connection = null;
        PreparedStatement deleteFlightStatement = null;
        PreparedStatement deleteTicketsStatement = null;

        try {
            connection = ConnectionManager.open();
            deleteFlightStatement = connection.prepareStatement(deleteFlightSql);
            deleteTicketsStatement = connection.prepareStatement(deleteTicketsSql);

            connection.setAutoCommit(false);

            deleteFlightStatement.setLong(1, 9L);
            deleteTicketsStatement.setLong(1, 9L);

            deleteTicketsStatement.executeUpdate();

            if (true) {
                throw new RuntimeException("Ooops");
            }

            deleteFlightStatement.executeUpdate();

            connection.commit();

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null){
                connection.close();
            }
            if (deleteFlightStatement != null){
                deleteFlightStatement.close();
            }
            if (deleteTicketsStatement != null) {
                deleteTicketsStatement.close();
            }
        }
    }
}
