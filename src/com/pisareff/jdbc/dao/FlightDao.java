package com.pisareff.jdbc.dao;

import com.pisareff.jdbc.dto.FlightFilter;
import com.pisareff.jdbc.dto.TicketFilter;
import com.pisareff.jdbc.entity.Flight;
import com.pisareff.jdbc.exception.DaoException;
import com.pisareff.jdbc.util.ConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class FlightDao implements Dao<Long, Flight>{
    private static final FlightDao INSTANCE = new FlightDao();
    private static final String DELETE_SQL = """
            DELETE FROM flight
            WHERE id = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO flight (flight_no, 
                    departure_date, 
                    departure_airport_code, 
                    arrival_date, 
                    arrival_airport_code, 
                    aircraft_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE flight
            SET flight_no = ?,
                departure_date = ?,
                departure_airport_code = ?,
                arrival_date = ?,
                arrival_airport_code = ?,
                aircraft_id = ?,
                status = ?
            WHERE id = ?
            """;

    private final static String FIND_BY_ID_SQL = """
            SELECT id, flight_no, departure_date,
                departure_airport_code,
                arrival_date,
                arrival_airport_code,
                aircraft_id,
                status 
            FROM flight
            WHERE id = ?
            """;

    private final static String FIND_ALL_SQL = """
            SELECT id, flight_no, departure_date,
                departure_airport_code,
                arrival_date,
                arrival_airport_code,
                aircraft_id,
                status
            FROM flight
            """;

    private FlightDao() {
    }

    public static FlightDao getInstance() {
        return INSTANCE;
    }

    public boolean delete(Long id) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(DELETE_SQL)) {

            prepareStatement.setLong(1, id);
            return prepareStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Flight save(Flight flight) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            prepareStatement.setString(1, flight.getFlightNo());
            prepareStatement.setTimestamp(2, Timestamp.valueOf(flight.getDepartureDate()));
            prepareStatement.setString(3, flight.getDepartureAirportCode());
            prepareStatement.setTimestamp(4, Timestamp.valueOf(flight.getArrivalDate()));
            prepareStatement.setString(5, flight.getArrivalAirportCode());
            prepareStatement.setInt(6, flight.getAircraftId());
            prepareStatement.setString(7, flight.getStatus());

            prepareStatement.executeUpdate();
            var resultSet = prepareStatement.getGeneratedKeys();
            if (resultSet.next()) {
                flight.setId(resultSet.getLong("id"));
            }

            return flight;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Flight flight) {
        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(UPDATE_SQL)) {

            prepareStatement.setString(1, flight.getFlightNo());
            prepareStatement.setTimestamp(2, Timestamp.valueOf(flight.getDepartureDate()));
            prepareStatement.setString(3, flight.getDepartureAirportCode());
            prepareStatement.setTimestamp(4, Timestamp.valueOf(flight.getArrivalDate()));
            prepareStatement.setString(5, flight.getArrivalAirportCode());
            prepareStatement.setInt(6, flight.getAircraftId());
            prepareStatement.setString(7, flight.getStatus());
            prepareStatement.setLong(8, flight.getId());

            prepareStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Flight> findById(Long id){
        try (var connection = ConnectionPool.get()){
            return findById(id, connection);
        }catch (SQLException e) {
            throw new DaoException(e);
        }
    }


    public Optional<Flight> findById(Long id,Connection connection) {
        try (var prepareStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            Flight flight = null;

            prepareStatement.setLong(1, id);
            var resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                flight = buildFlight(resultSet);
            }

            return Optional.ofNullable(flight);

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Flight> findAll() {

        List<Flight> flights = new ArrayList<>();

        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = prepareStatement.executeQuery();

            while (resultSet.next()) {
                flights.add(buildFlight(resultSet));
            }

            return flights;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Flight> findAll(FlightFilter filter) {
        List<Flight> resultFlights = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();

        if (filter.flightNo() != null) {
            parameters.add(filter.flightNo());
            whereSql.add("flight_no = ?");
        }

        if (filter.status() != null) {
            parameters.add(filter.status());
            whereSql.add("status = ?");
        }

        parameters.add(filter.limit());
        parameters.add(filter.offset());

        String where = whereSql.stream().collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ?"));

        String filtredFindAllSql = FIND_ALL_SQL + where;

        try (var connection = ConnectionPool.get();
             var prepareStatement = connection.prepareStatement(filtredFindAllSql)) {

            for (int i = 0; i < parameters.size(); i++) {
                prepareStatement.setObject(i + 1, parameters.get(i));
            }

            ResultSet resultSet = prepareStatement.executeQuery();
            while (resultSet.next()){
                resultFlights.add(buildFlight(resultSet));
            }

            return resultFlights;

        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }

    private Flight buildFlight(ResultSet resultSet) throws SQLException {
        return new Flight(
                resultSet.getLong("id"),
                resultSet.getString("flight_no"),
                resultSet.getTimestamp("departure_date").toLocalDateTime(),
                resultSet.getString("departure_airport_code"),
                resultSet.getTimestamp("arrival_date").toLocalDateTime(),
                resultSet.getString("arrival_airport_code"),
                resultSet.getInt("aircraft_id"),
                resultSet.getString("status"));

    }

}