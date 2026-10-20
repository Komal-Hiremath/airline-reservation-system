package org.qsp.airportmanagement.repository;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.springframework.http.HttpStatus;

import org.qsp.airportmanagement.model.BookingStatus;
import org.qsp.airportmanagement.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /** JPQL: number of seats taken on a flight by bookings with the given status. */
    @Query("""
            SELECT COUNT(p) FROM Passenger p
            WHERE p.booking.flight.id = :flightId
              AND p.booking.status = :status
            """)
    long countBookedSeats(@Param("flightId") Long flightId,
                          @Param("status") BookingStatus status);

    /**
     * Native SQL: passenger list of a flight (confirmed bookings only).
     * Columns: passenger id, name, age, gender, booking id, booked-by name.
     */
    @Query(value = """
            SELECT p.id, p.name, p.age, p.gender, b.id AS booking_id, u.name AS booked_by
            FROM passenger p
            JOIN bookings b ON b.id = p.booking_id
            JOIN app_user u ON u.id = b.user_id
            WHERE b.flight_id = :flightId
              AND b.status = 'CONFIRMED'
            ORDER BY p.id
            """, nativeQuery = true)
    List<Object[]> findPassengerRowsByFlightId(@Param("flightId") Long flightId);



    default ResponseStructure<List<Object[]>> findPassengerRowsByFlightResponse(Long flightId) {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Passenger rows loaded from database", findPassengerRowsByFlightId(flightId));
    }

    default ResponseStructure<Long> countBookedSeatsResponse(Long flightId, BookingStatus status) {
        return ResponseStructure.success(HttpStatus.OK.value(),
                "Booked seat count loaded from database", countBookedSeats(flightId, status));
    }

}
