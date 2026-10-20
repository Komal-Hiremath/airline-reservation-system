package org.qsp.airportmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.qsp.airportmanagement.dto.FlightOccupancyResponse;
import org.qsp.airportmanagement.dto.FlightPassengerResponse;
import org.qsp.airportmanagement.dto.FlightRequest;
import org.qsp.airportmanagement.dto.FlightResponse;
import org.qsp.airportmanagement.dto.PageResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.service.FlightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/flights")
@Tag(name = "Flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @Operation(summary = "Search flights by route and date (public, paged, sorted)",
            description = "sort format: field,direction  e.g. price,asc or departureTime,desc")
    @GetMapping("/search")
    public ResponseEntity<ResponseStructure<PageResponse<FlightResponse>>> search(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime,asc") String sort) {
        ResponseStructure<PageResponse<FlightResponse>> response =
                flightService.searchFlights(from, to, date, page, size, sort);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Browse all upcoming flights (public, paged, sorted)")
    @GetMapping
    public ResponseEntity<ResponseStructure<PageResponse<FlightResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime,asc") String sort) {
        ResponseStructure<PageResponse<FlightResponse>> response =
                flightService.listUpcomingFlights(page, size, sort);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Flight details (public)")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseStructure<FlightResponse>> getFlight(@PathVariable Long id) {
        ResponseStructure<FlightResponse> response = flightService.getFlightById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Add a flight (admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<FlightResponse>> createFlight(
            @Valid @RequestBody FlightRequest request) {
        ResponseStructure<FlightResponse> response = flightService.createFlight(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Update a flight (admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<FlightResponse>> updateFlight(
            @PathVariable Long id, @Valid @RequestBody FlightRequest request) {
        ResponseStructure<FlightResponse> response = flightService.updateFlight(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Deactivate a flight (admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<Void>> deactivateFlight(@PathVariable Long id) {
        ResponseStructure<Void> response = flightService.deactivateFlight(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Passenger list of a flight (admin)")
    @GetMapping("/{id}/passengers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<List<FlightPassengerResponse>>> getPassengers(
            @PathVariable Long id) {
        ResponseStructure<List<FlightPassengerResponse>> response = flightService.getPassengers(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Booked vs total seats (admin)")
    @GetMapping("/{id}/occupancy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<FlightOccupancyResponse>> getOccupancy(
            @PathVariable Long id) {
        ResponseStructure<FlightOccupancyResponse> response = flightService.getOccupancy(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
