package org.qsp.airportmanagement.service;

import org.qsp.airportmanagement.dto.FlightMapper;
import org.qsp.airportmanagement.dto.FlightOccupancyResponse;
import org.qsp.airportmanagement.dto.FlightPassengerResponse;
import org.qsp.airportmanagement.dto.FlightRequest;
import org.qsp.airportmanagement.dto.FlightResponse;
import org.qsp.airportmanagement.dto.PageResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.exception.ConflictException;
import org.qsp.airportmanagement.exception.ResourceNotFoundException;
import org.qsp.airportmanagement.model.Airport;
import org.qsp.airportmanagement.model.BookingStatus;
import org.qsp.airportmanagement.model.Flight;
import org.qsp.airportmanagement.repository.AirportRepository;
import org.qsp.airportmanagement.repository.FlightRepository;
import org.qsp.airportmanagement.repository.PassengerRepository;
import org.qsp.airportmanagement.util.PageableFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class FlightService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "flightNumber", "departureTime", "arrivalTime",
            "price");

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final PassengerRepository passengerRepository;
    private final FlightMapper flightMapper;

    public FlightService(FlightRepository flightRepository,
            AirportRepository airportRepository,
            PassengerRepository passengerRepository,
            FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.airportRepository = airportRepository;
        this.passengerRepository = passengerRepository;
        this.flightMapper = flightMapper;
    }

    // ---------------------------------------------------------------- admin writes

    @Transactional
    public ResponseStructure<FlightResponse> createFlight(FlightRequest request) {
        validate(request);

        Airport origin = activeAirport(request.originCode());
        Airport destination = activeAirport(request.destinationCode());

        Flight saved = flightRepository.save(flightMapper.toEntity(request, origin, destination));
        return ResponseStructure.success(HttpStatus.CREATED.value(), "Flight created successfully",
                flightMapper.toResponse(saved));
    }

    @Transactional
    public ResponseStructure<FlightResponse> updateFlight(Long id, FlightRequest request) {
        validate(request);

        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));

        if (!flight.isActive()) {
            throw new ConflictException("Flight " + id + " is deactivated and cannot be edited");
        }

        // Seats that are already sold must stay sold when the capacity changes.
        int bookedSeats = flight.getTotalSeats() - flight.getAvailableSeats();
        if (request.totalSeats() < bookedSeats) {
            throw new ConflictException("Cannot reduce seats to " + request.totalSeats()
                    + ": " + bookedSeats + " seats are already booked");
        }

        Airport origin = activeAirport(request.originCode());
        Airport destination = activeAirport(request.destinationCode());

        flightMapper.apply(request, flight, origin, destination);
        flight.setAvailableSeats(request.totalSeats() - bookedSeats);

        return ResponseStructure.success(HttpStatus.OK.value(), "Flight updated successfully",
                flightMapper.toResponse(flightRepository.save(flight)));
    }

    /**
     * DELETE /flights/{id}: the flight is hidden from customers, existing bookings
     * are kept.
     */
    @Transactional
    public ResponseStructure<Void> deactivateFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));

        flight.setActive(false);
        flightRepository.save(flight);
        return ResponseStructure.success(HttpStatus.OK.value(), "Flight deactivated successfully", null);
    }

    // ---------------------------------------------------------------- public reads

    
@Transactional(readOnly = true)
public ResponseStructure<PageResponse<FlightResponse>> searchFlights(
        String from, String to, LocalDate date, int page, int size, String sort) {

    if (from == null || from.isBlank() || to == null || to.isBlank()) {
        throw new IllegalArgumentException(
                "Both 'from' and 'to' airport codes are required");
    }

    if (from.trim().equalsIgnoreCase(to.trim())) {
        throw new IllegalArgumentException(
                "Origin and destination must be different");
    }

    if (date == null) {
        throw new IllegalArgumentException("A travel date is required");
    }

    Pageable pageable = PageableFactory.of(page, size, sort, SORT_FIELDS);

    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now().withNano(0);

    LocalDateTime startDateTime = date.atStartOfDay();

    // Never return flights that have already departed today.
    if (date.equals(today) && now.isAfter(startDateTime)) {
        startDateTime = now;
    }

    LocalDateTime endDateTime = date.atTime(23, 59, 59);

    // Match the format stored in the database: yyyy-MM-dd HH:mm:ss
    java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    String start = startDateTime.format(formatter);
    String end = endDateTime.format(formatter);

    return ResponseStructure.success(HttpStatus.OK.value(), "Flights searched successfully",
            PageResponse.of(
                    flightRepository
                            .searchFlightsResponse(
                                    from.trim().toUpperCase(),
                                    to.trim().toUpperCase(),
                                    start,
                                    end,
                                    pageable)
                            .getData()
                            .map(flightMapper::toResponse)));
}

    @Transactional(readOnly = true)
    public ResponseStructure<PageResponse<FlightResponse>> listUpcomingFlights(int page, int size, String sort) {
        Pageable pageable = PageableFactory.of(page, size, sort, SORT_FIELDS);

        return ResponseStructure.success(HttpStatus.OK.value(), "Upcoming flights retrieved successfully",
                PageResponse.of(flightRepository
                        .findUpcomingFlightsResponse(LocalDateTime.now().toString(), pageable)
                        .getData()
                        .map(flightMapper::toResponse)));
    }

    @Transactional(readOnly = true)
    public ResponseStructure<FlightResponse> getFlightById(Long id) {
        Flight flight = flightRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));
        return ResponseStructure.success(HttpStatus.OK.value(), "Flight retrieved successfully",
                flightMapper.toResponse(flight));
    }

    // ---------------------------------------------------------------- admin reads

    @Transactional(readOnly = true)
    public ResponseStructure<List<FlightPassengerResponse>> getPassengers(Long flightId) {
        requireFlight(flightId);

        List<FlightPassengerResponse> passengers = passengerRepository
                .findPassengerRowsByFlightResponse(flightId).getData().stream()
                .map(row -> new FlightPassengerResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        String.valueOf(row[3]),
                        ((Number) row[4]).longValue(),
                        (String) row[5]))
                .toList();
        return ResponseStructure.success(HttpStatus.OK.value(), "Flight passengers retrieved successfully", passengers);
    }

    @Transactional(readOnly = true)
    public ResponseStructure<FlightOccupancyResponse> getOccupancy(Long flightId) {
        Flight flight = requireFlight(flightId);

        long booked = passengerRepository
                .countBookedSeatsResponse(flightId, BookingStatus.CONFIRMED).getData();
        int total = flight.getTotalSeats();
        double percentage = total == 0 ? 0.0 : Math.round(booked * 1000.0 / total) / 10.0;

        return ResponseStructure.success(HttpStatus.OK.value(), "Flight occupancy retrieved successfully",
                new FlightOccupancyResponse(
                        flight.getId(), flight.getFlightNumber(), total, booked, total - booked, percentage));
    }

    // ---------------------------------------------------------------- helpers

    private Flight requireFlight(Long id) {
        return flightRepository.findWithAirportsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));
    }

    private Airport activeAirport(String code) {
        return airportRepository.findByCodeAndActiveTrue(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Airport not found with code: " + code.trim().toUpperCase()));
    }

    /** Rule 1: origin and destination differ. Plus basic time sanity. */
    private void validate(FlightRequest request) {
        if (request.originCode().trim()
                .equalsIgnoreCase(request.destinationCode().trim())) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different");
        }

        String departure = request.departureTime();
        String arrival = request.arrivalTime();

        if (arrival.compareTo(departure) <= 0) {
            throw new IllegalArgumentException(
                    "Arrival time must be after departure time");
        }

        if (departure.compareTo(LocalDateTime.now().toString()) <= 0) {
            throw new IllegalArgumentException(
                    "Departure time must be in the future");
        }
    }
}