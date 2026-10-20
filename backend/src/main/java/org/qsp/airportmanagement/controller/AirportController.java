package org.qsp.airportmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.qsp.airportmanagement.dto.AirportRequest;
import org.qsp.airportmanagement.dto.AirportResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.service.AirportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airports")
@Tag(name = "Airports")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @Operation(summary = "List active airports (public)")
    @GetMapping
    public ResponseEntity<ResponseStructure<List<AirportResponse>>> getAllAirports() {
        ResponseStructure<List<AirportResponse>> response = airportService.getAllAirports();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Add an airport (admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<AirportResponse>> createAirport(
            @Valid @RequestBody AirportRequest request) {
        ResponseStructure<AirportResponse> response = airportService.createAirport(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Update an airport (admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<AirportResponse>> updateAirport(
            @PathVariable Long id, @Valid @RequestBody AirportRequest request) {
        ResponseStructure<AirportResponse> response = airportService.updateAirport(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Deactivate an airport (admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<Void>> deactivateAirport(@PathVariable Long id) {
        ResponseStructure<Void> response = airportService.deactivateAirport(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
