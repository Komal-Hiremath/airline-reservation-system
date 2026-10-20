package org.qsp.airportmanagement.service;

import org.qsp.airportmanagement.dto.AirportMapper;
import org.qsp.airportmanagement.dto.AirportRequest;
import org.qsp.airportmanagement.dto.AirportResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.exception.ConflictException;
import org.qsp.airportmanagement.exception.ResourceNotFoundException;
import org.qsp.airportmanagement.model.Airport;
import org.qsp.airportmanagement.repository.AirportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    public AirportService(AirportRepository airportRepository, AirportMapper airportMapper) {
        this.airportRepository = airportRepository;
        this.airportMapper = airportMapper;
    }

    @Transactional(readOnly = true)
    public ResponseStructure<List<AirportResponse>> getAllAirports() {
        ResponseStructure<List<Airport>> repositoryResponse =
                airportRepository.findActiveAirportsResponse();
        List<AirportResponse> airports = repositoryResponse.getData().stream()
                .map(airportMapper::toResponse)
                .toList();

        return ResponseStructure.success(HttpStatus.OK.value(),
                "Airports retrieved successfully", airports);
    }

    @Transactional
    public ResponseStructure<AirportResponse> createAirport(AirportRequest request) {
        String code = request.code().trim().toUpperCase();

        if (airportRepository.existsByCode(code)) {
            throw new ConflictException("An airport with code " + code + " already exists");
        }

        Airport saved = airportRepository.save(airportMapper.toEntity(request));
        return ResponseStructure.success(HttpStatus.CREATED.value(),
                "Airport created successfully", airportMapper.toResponse(saved));
    }

    @Transactional
    public ResponseStructure<AirportResponse> updateAirport(Long id, AirportRequest request) {
        Airport airport = findActive(id);
        String newCode = request.code().trim().toUpperCase();

        if (!newCode.equals(airport.getCode()) && airportRepository.existsByCode(newCode)) {
            throw new ConflictException("An airport with code " + newCode + " already exists");
        }

        airportMapper.apply(request, airport);
        Airport saved = airportRepository.save(airport);
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Airport updated successfully", airportMapper.toResponse(saved));
    }

    /** DELETE /airports/{id} deactivates the airport. */
    @Transactional
    public ResponseStructure<Void> deactivateAirport(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found with ID: " + id));

        if (airport.isActive()) {
            if (airportRepository.hasActiveFlights(id)) {
                throw new ConflictException(
                        "Airport " + airport.getCode() + " still has active flights. Deactivate them first");
            }

            airport.setActive(false);
            airportRepository.save(airport);
        }

        return ResponseStructure.success(HttpStatus.OK.value(),
                "Airport deactivated successfully", null);
    }

    private Airport findActive(Long id) {
        return airportRepository.findById(id)
                .filter(Airport::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found with ID: " + id));
    }
}
