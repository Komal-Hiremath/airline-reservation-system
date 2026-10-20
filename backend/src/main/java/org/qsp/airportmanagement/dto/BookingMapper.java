package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Booking;
import org.qsp.airportmanagement.model.Passenger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    private final FlightMapper flightMapper;

    public BookingMapper(FlightMapper flightMapper) {
        this.flightMapper = flightMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        List<PassengerResponse> passengers = booking.getPassengers().stream()
                .map(this::toPassengerResponse)
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                flightMapper.toResponse(booking.getFlight()),
                passengers);
    }

    private PassengerResponse toPassengerResponse(Passenger passenger) {
        return new PassengerResponse(
                passenger.getId(),
                passenger.getName(),
                passenger.getAge(),
                passenger.getGender());
    }
}
