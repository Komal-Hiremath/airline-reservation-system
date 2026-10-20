package org.qsp.airportmanagement.repository;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.springframework.http.HttpStatus;

import org.qsp.airportmanagement.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    List<Airport> findByActiveTrueOrderByCityAscCodeAsc();

    Optional<Airport> findByCodeAndActiveTrue(String code);

    Optional<Airport> findByCode(String code);

    boolean existsByCode(String code);

    /** True when at least one active flight departs from or arrives at this airport. */
    @Query("""
            SELECT COUNT(f) > 0 FROM Flight f
            WHERE f.active = true
              AND (f.origin.id = :airportId OR f.destination.id = :airportId)
            """)
    boolean hasActiveFlights(@Param("airportId") Long airportId);



    /** Wrapped repository-layer read; standard Spring Data methods remain unchanged. */
    default ResponseStructure<List<Airport>> findActiveAirportsResponse() {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Active airports loaded from database", findByActiveTrueOrderByCityAscCodeAsc());
    }

    default ResponseStructure<Airport> findActiveAirportByCodeResponse(String code) {
        return findByCodeAndActiveTrue(code)
                .map(airport -> ResponseStructure.success(HttpStatus.OK.value(),
                        "Airport loaded from database", airport))
                .orElseGet(() -> ResponseStructure.failure(HttpStatus.NOT_FOUND.value(),
                        "Airport not found with code: " + code));
    }

}
