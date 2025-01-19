package com.pisareff.jdbc;

import com.pisareff.jdbc.dao.FlightDao;
import com.pisareff.jdbc.dao.TicketDao;
import com.pisareff.jdbc.dto.FlightFilter;
import com.pisareff.jdbc.dto.TicketFilter;
import com.pisareff.jdbc.entity.Flight;
import com.pisareff.jdbc.entity.Ticket;
import com.pisareff.jdbc.util.ConnectionPool;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DaoRunner {
    public static void main(String[] args) {

        var ticketDao = TicketDao.getInstance();
        List<Ticket> tickets = ticketDao.findAll();

        for (Ticket ticket : tickets){
            System.out.println(ticket);
        }

    }

    private static void findAllFlights() {
        FlightDao flightDao = FlightDao.getInstance();
        List<Flight> flights = flightDao.findAll();
        for(Flight flight : flights){
            System.out.println(flight);
        }
    }

    private static void getFlightById() {
        FlightDao flightDao = FlightDao.getInstance();
        var maybeFlight = flightDao.findById(11L);
        Flight flight = maybeFlight.orElse(new Flight());
        System.out.println(flight);
    }

    private static void saveFlight() {
        Flight flight = new Flight(0L,
                "MN1488",
                LocalDateTime.of(2024,1,20,21,30),
                "MSK",
                LocalDateTime.of(2024,1,21,6,0),
                "BSL",
                3,
                "SCHEDULED");

        System.out.println(flight);

        FlightDao flightDao = FlightDao.getInstance();
        flightDao.save(flight);

        System.out.println(flight);
    }

    private static void findAllWithFilterTest() {
        TicketFilter filter = new TicketFilter(3, 0, "Евгений Кудрявцев", "A1");
        List<Ticket> tickets = TicketDao.getInstance().findAll(filter);
        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
    }

    private static void findAllTest() {
        List<Ticket> tickets = TicketDao.getInstance().findAll();

        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                System.out.println(ticket);
            }
        }
    }

    private static void findByIdAndUpdateCostTest() {
        TicketDao ticketDao = TicketDao.getInstance();
        var maybeTicket = ticketDao.findById(2L);
        System.out.println(maybeTicket);
        maybeTicket.ifPresent(ticket -> {
            ticket.setCost(BigDecimal.valueOf(188.88));
            ticketDao.update(ticket);
        });
    }

    private static void deleteTest() {
        var ticketDao = TicketDao.getInstance();
        var deleteResult = ticketDao.delete(56L);
        System.out.println(deleteResult);
    }

    private static void saveTest() {
        var ticketDao = TicketDao.getInstance();
        var ticket = new Ticket();

        ticket.setPassengerNo("192168aE");
        ticket.setPassengerName("Test");
        //ticket.setFlight(new Flight());
        ticket.setSeatNo("B3");
        ticket.setCost(BigDecimal.valueOf(1488));

        var savedTicket = ticketDao.save(ticket);
        System.out.println(savedTicket);
    }
}
