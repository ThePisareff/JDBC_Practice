package com.pisareff.jdbc.dao;

import com.pisareff.jdbc.dto.TicketFilter;
import com.pisareff.jdbc.entity.Ticket;
import com.pisareff.jdbc.exception.DaoException;
import com.pisareff.jdbc.util.ConnectionManager;
import com.pisareff.jdbc.util.ConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

// DAO - Data Access Object
// Делаем его как singletone
// Правда хороший вопрос. Зачем? Можно объявить все методы статичными ибо юзаются лишь константы
public class TicketDao implements Dao<Long, Ticket> {

    private static final TicketDao INSTANCE = new TicketDao();
    private static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE  id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO ticket (passenger_no, passenger_name, flight_id, seat_no, cost)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_no = ?,
                passenger_name = ?,
                flight_id = ?,
                seat_no = ?,
                cost = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id,
                   passenger_no,
                   passenger_name,
                   flight_id,
                   seat_no,
                   cost
            FROM ticket
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id,
                   passenger_no,
                   passenger_name,
                   flight_id,
                   seat_no,
                   cost
            FROM ticket
            WHERE id = ?
            """;


    private TicketDao() {
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }

    public boolean delete(Long id) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(DELETE_SQL);) {

            prepareStatement.setLong(1, id);
            return prepareStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Ticket save(Ticket ticket) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(SAVE_SQL);) {

            prepareStatement.setString(1, ticket.getPassengerNo());
            prepareStatement.setString(2, ticket.getPassengerName());
            prepareStatement.setLong(3, ticket.getFlight().getId());
            prepareStatement.setString(4, ticket.getSeatNo());
            prepareStatement.setBigDecimal(5, ticket.getCost());

            prepareStatement.executeUpdate();

            var generatedKeys = prepareStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                ticket.setId(generatedKeys.getLong("id"));
            }

            return ticket;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Ticket ticket) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(UPDATE_SQL, Statement.RETURN_GENERATED_KEYS);) {
            prepareStatement.setString(1, ticket.getPassengerNo());
            prepareStatement.setString(2, ticket.getPassengerName());
            prepareStatement.setLong(3, ticket.getFlight().getId());
            prepareStatement.setString(4, ticket.getSeatNo());
            prepareStatement.setBigDecimal(5, ticket.getCost());
            prepareStatement.setLong(6, ticket.getId());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Что бы не возвращать null возвращаем Optional
    public Optional<Ticket> findById(Long id) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            prepareStatement.setLong(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();
            Ticket ticket = null;
            if (resultSet.next()) {
                ticket = buildTicket(resultSet);
            }
            return Optional.ofNullable(ticket);

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Ticket> findAll() {
        List<Ticket> resultList = new ArrayList<>();

        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(FIND_ALL_SQL)) {

            var resultSet = prepareStatement.executeQuery();

            while (resultSet.next()) {
                resultList.add(buildTicket(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }

        return resultList;

    }

    public List<Ticket> findAll(TicketFilter filter) {

        List<Ticket> resultTicketList = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.seatNo() != null) {
            whereSql.add("seat_no LIKE ?");
            parameters.add("%" + filter.seatNo() + "%");
        }
        if (filter.passengerName() != null) {
            whereSql.add("passenger_name = ?");
            parameters.add(filter.passengerName());
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());

        var where = whereSql.stream()
                .collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ?"));

        String sql = FIND_ALL_SQL + where;

        System.out.println(sql);

        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++) {
                prepareStatement.setObject(i + 1, parameters.get(i));
            }

            var resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                resultTicketList.add(buildTicket(resultSet));
            }

            return resultTicketList;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Ticket buildTicket(ResultSet resultSet) throws SQLException {
        return new Ticket(resultSet.getLong("id"),
                resultSet.getString("passenger_no"),
                resultSet.getString("passenger_name"),
                FlightDao.getInstance().findById(resultSet.getLong("flight_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                resultSet.getString("seat_no"),
                resultSet.getBigDecimal("cost"));
    }
}