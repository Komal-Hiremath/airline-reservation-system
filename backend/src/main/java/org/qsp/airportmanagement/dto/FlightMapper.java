package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Airport;
import org.qsp.airportmanagement.model.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    private final AirportMapper airportMapper;

    public FlightMapper(AirportMapper airportMapper) {
        this.airportMapper = airportMapper;
    }

    public Flight toEntity(FlightRequest request, Airport origin, Airport destination) {
        Flight flight = new Flight();
        apply(request, flight, origin, destination);
        flight.setAvailableSeats(request.totalSeats());
        flight.setActive(true);
        return flight;
    }

    /** Copies the editable fields. Seat counts are handled by FlightService. */
    public void apply(FlightRequest request, Flight flight, Airport origin, Airport destination) {
        flight.setFlightNumber(request.flightNumber().trim().toUpperCase());
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setPrice(request.price());
        flight.setTotalSeats(request.totalSeats());
    }

    public FlightResponse toResponse(Flight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getFlightNumber(),
                airportMapper.toResponse(flight.getOrigin()),
                airportMapper.toResponse(flight.getDestination()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                flight.isActive());
    }
}
