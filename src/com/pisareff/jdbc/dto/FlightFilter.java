package com.pisareff.jdbc.dto;

public record FlightFilter(int limit,
                           int offset,
                           String flightNo,
                           String status) {}
