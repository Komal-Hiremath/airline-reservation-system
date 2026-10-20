package org.qsp.airportmanagement.repository;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.springframework.http.HttpStatus;

import jakarta.persistence.LockModeType;
import org.qsp.airportmanagement.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Query 1 of the paged history: only the bookings of the current page. */
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    /**
     * Query 2 of the paged history. JOIN FETCH loads flight, airports and passengers
     * for all bookings of the page in one go, which avoids the N+1 problem.
     * (Collections are not fetched together with paging, because that would page in memory.)
     */
    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN FETCH b.flight f
            JOIN FETCH f.origin
            JOIN FETCH f.destination
            LEFT JOIN FETCH b.passengers
            WHERE b.id IN :ids
            """)
    List<Booking> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN FETCH b.flight f
            JOIN FETCH f.origin
            JOIN FETCH f.destination
            LEFT JOIN FETCH b.passengers
            WHERE b.id = :id
            """)
    Optional<Booking> findDetailedById(@Param("id") Long id);

    /** Locks the booking row so two simultaneous cancel requests cannot both succeed. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);



    /** Wrapped repository-layer read; Spring Data paging methods keep their native signatures. */
    default ResponseStructure<Page<Booking>> findBookingsByUserResponse(
            Long userId, Pageable pageable) {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Bookings loaded from database", findByUserId(userId, pageable));
    }

    default ResponseStructure<Booking> findDetailedBookingResponse(Long id) {
        return findDetailedById(id)
                .map(booking -> ResponseStructure.success(HttpStatus.OK.value(),
                        "Booking loaded from database", booking))
                .orElseGet(() -> ResponseStructure.failure(HttpStatus.NOT_FOUND.value(),
                        "Booking not found with ID: " + id));
    }

}
