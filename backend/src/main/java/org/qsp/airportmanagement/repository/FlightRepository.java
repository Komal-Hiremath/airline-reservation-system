package org.qsp.airportmanagement.repository;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.springframework.http.HttpStatus;

import jakarta.persistence.LockModeType;
import org.qsp.airportmanagement.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    /** Derived query used by the public flight search (paged and sorted). */
    @EntityGraph(attributePaths = {"origin", "destination"})
    Page<Flight> findByOriginCodeAndDestinationCodeAndDepartureTimeBetweenAndActiveTrue(
            String originCode,
            String destinationCode,
            String start,
            String end,
            Pageable pageable);

    /** Upcoming active flights, used for the browse-all list. */
    @EntityGraph(attributePaths = {"origin", "destination"})
    Page<Flight> findByActiveTrueAndDepartureTimeAfter(String now, Pageable pageable);

    @EntityGraph(attributePaths = {"origin", "destination"})
    Optional<Flight> findByIdAndActiveTrue(Long id);

    /** Includes inactive flights; used by admin screens. */
    @EntityGraph(attributePaths = {"origin", "destination"})
    Optional<Flight> findWithAirportsById(Long id);

    /** Defined as @NamedQuery on the Flight entity ("Flight.findActiveByOriginCode"). */
    List<Flight> findActiveByOriginCode(@Param("code") String code);

    /** Row lock so two customers can never take the same last seat. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.id = :id AND f.active = true")
    Optional<Flight> findByIdForBooking(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.id = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);



    /** Wrapped repository-layer search; the underlying Spring Data query remains unchanged. */
    default ResponseStructure<Page<Flight>> searchFlightsResponse(
            String originCode, String destinationCode, String start, String end, Pageable pageable) {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Flights loaded from database",
                findByOriginCodeAndDestinationCodeAndDepartureTimeBetweenAndActiveTrue(
                        originCode, destinationCode, start, end, pageable));
    }

    default ResponseStructure<Page<Flight>> findUpcomingFlightsResponse(
            String now, Pageable pageable) {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Upcoming flights loaded from database",
                findByActiveTrueAndDepartureTimeAfter(now, pageable));
    }

    default ResponseStructure<Flight> findActiveFlightResponse(Long id) {
        return findByIdAndActiveTrue(id)
                .map(flight -> ResponseStructure.success(HttpStatus.OK.value(),
                        "Flight loaded from database", flight))
                .orElseGet(() -> ResponseStructure.failure(HttpStatus.NOT_FOUND.value(),
                        "Flight not found with ID: " + id));
    }

}