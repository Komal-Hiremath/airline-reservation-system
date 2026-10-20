package org.qsp.airportmanagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qsp.airportmanagement.dto.BookingRequest;
import org.qsp.airportmanagement.dto.BookingResponse;
import org.qsp.airportmanagement.dto.PassengerRequest;
import org.qsp.airportmanagement.exception.NoSeatsAvailableException;
import org.qsp.airportmanagement.model.*;
import org.qsp.airportmanagement.repository.*;
import org.qsp.airportmanagement.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the PRD rules for bookings: seats go down on booking and up on cancel,
 * no overbooking, own bookings only, and a failed booking leaves no partial
 * data.
 * These tests commit for real (no @Transactional on the class) so rollback is
 * genuine.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingServiceTest {

    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    FlightRepository flightRepository;
    @Autowired
    AirportRepository airportRepository;
    @Autowired
    UserRepository userRepository;

    private Flight flight;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Airport blr = airport("BLR", "Bengaluru");
        Airport del = airport("DEL", "Delhi");

        flight = new Flight();
        flight.setFlightNumber("TS100");
        flight.setOrigin(blr);
        flight.setDestination(del);
        flight.setDepartureTime(
                LocalDateTime.now().plusDays(2).withNano(0).toString());
        flight.setArrivalTime(
                LocalDateTime.now().plusDays(2).plusHours(3)
                        .withNano(0).toString());
        flight.setPrice(new BigDecimal("1000.00"));
        flight.setTotalSeats(5);
        flight.setAvailableSeats(5);
        flight = flightRepository.save(flight);

        user("alice@test.com");
        user("bob@test.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        cleanDatabase();
    }

    @Test
    void booking_reduces_seats_and_cancel_gives_them_back() {
        loginAs("alice@test.com");

        BookingResponse booking = bookingService.createBooking(request(passenger("Asha"), passenger("Ravi"))).getData();

        assertEquals(BookingStatus.CONFIRMED, booking.status());
        assertEquals(0, new BigDecimal("2000.00").compareTo(booking.totalAmount()));
        assertEquals(3, seatsLeft());

        BookingResponse cancelled = bookingService.cancelBooking(booking.id()).getData();

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertEquals(5, seatsLeft());
    }

    @Test
    void cannot_book_more_seats_than_available() {
        loginAs("alice@test.com");

        assertThrows(NoSeatsAvailableException.class, () -> bookingService.createBooking(request(
                passenger("A"), passenger("B"), passenger("C"),
                passenger("D"), passenger("E"), passenger("F"))));

        assertEquals(5, seatsLeft());
    }

    @Test
    void customer_cannot_see_or_cancel_someone_elses_booking() {
        loginAs("alice@test.com");
        Long bookingId = bookingService.createBooking(request(passenger("Asha"))).getData().id();

        loginAs("bob@test.com");

        assertThrows(AccessDeniedException.class, () -> bookingService.getBookingById(bookingId));
        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(bookingId));
        assertEquals(4, seatsLeft());
    }

    /**
     * Rollback demonstration. The passenger name is far longer than the database
     * column,
     * so the insert fails AFTER the seats were already reduced in memory.
     * Because everything is one @Transactional method, the seat change is rolled
     * back too.
     */
    @Test
    void failed_booking_leaves_no_partial_data() {
        loginAs("alice@test.com");
        String tooLong = "x".repeat(400);

        assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(request(passenger(tooLong))));

        assertEquals(5, seatsLeft(), "seats must be unchanged after the failure");
        assertEquals(0, bookingRepository.count(), "no booking may be left behind");
    }

    // ---------------------------------------------------------------- helpers

    private int seatsLeft() {
        return flightRepository.findById(flight.getId()).orElseThrow().getAvailableSeats();
    }

    private BookingRequest request(PassengerRequest... passengers) {
        return new BookingRequest(flight.getId(), List.of(passengers));
    }

    private PassengerRequest passenger(String name) {
        return new PassengerRequest(name, 30, Gender.OTHER);
    }

    private Airport airport(String code, String city) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(city + " Airport");
        airport.setCity(city);
        return airportRepository.save(airport);
    }

    private void user(String email) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("not-used-in-this-test");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
    }

    private void loginAs(String email) {
        var principal = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("x")
                .authorities(AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private void cleanDatabase() {
        bookingRepository.deleteAll();
        flightRepository.deleteAll();
        airportRepository.deleteAll();
        userRepository.deleteAll();
    }
}
