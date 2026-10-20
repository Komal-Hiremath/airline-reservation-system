package org.qsp.airportmanagement.service;

import org.qsp.airportmanagement.dto.BookingMapper;
import org.qsp.airportmanagement.dto.BookingRequest;
import org.qsp.airportmanagement.dto.BookingResponse;
import org.qsp.airportmanagement.dto.PageResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.dto.PassengerRequest;
import org.qsp.airportmanagement.exception.ConflictException;
import org.qsp.airportmanagement.exception.NoSeatsAvailableException;
import org.qsp.airportmanagement.exception.ResourceNotFoundException;
import org.qsp.airportmanagement.model.Booking;
import org.qsp.airportmanagement.model.BookingStatus;
import org.qsp.airportmanagement.model.Flight;
import org.qsp.airportmanagement.model.Passenger;
import org.qsp.airportmanagement.model.User;
import org.qsp.airportmanagement.repository.BookingRepository;
import org.qsp.airportmanagement.repository.FlightRepository;
import org.qsp.airportmanagement.repository.UserRepository;
import org.qsp.airportmanagement.util.PageableFactory;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "createdAt", "totalAmount");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final BookingMapper bookingMapper;
    private final FareService fareService;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          FlightRepository flightRepository,
                          BookingMapper bookingMapper,
                          FareService fareService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.bookingMapper = bookingMapper;
        this.fareService = fareService;
    }

    /**
     * Creates the booking and takes the seats in ONE transaction.
     * If anything fails after the seats are reduced, everything is rolled back.
     */
    @Transactional
    public ResponseStructure<BookingResponse> createBooking(BookingRequest request) {
        User user = getLoggedInUser();

        // Row lock: concurrent bookings for the same flight queue up here.
        Flight flight = flightRepository.findByIdForBooking(request.flightId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Flight not found with ID: " + request.flightId()));

       if (flight.getDepartureTime().compareTo(LocalDateTime.now().toString()) <= 0) {
    throw new ConflictException("This flight has already departed");
}

        int seatsNeeded = request.passengers().size();
        if (flight.getAvailableSeats() < seatsNeeded) {
            throw new NoSeatsAvailableException(seatsNeeded, flight.getAvailableSeats());
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setFlight(flight);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(fareService.totalFor(flight.getPrice(), seatsNeeded));

        for (PassengerRequest p : request.passengers()) {
            Passenger passenger = new Passenger();
            passenger.setName(p.name().trim());
            passenger.setAge(p.age());
            passenger.setGender(p.gender());
            booking.addPassenger(passenger);
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - seatsNeeded);

        Booking saved = bookingRepository.save(booking);
        return ResponseStructure.success(HttpStatus.CREATED.value(), "Booking created successfully",
                bookingMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public ResponseStructure<PageResponse<BookingResponse>> getMyBookings(int page, int size, String sort) {
        User user = getLoggedInUser();
        Pageable pageable = PageableFactory.of(page, size, sort, SORT_FIELDS);

        // Query 1: just the bookings of this page.
        ResponseStructure<Page<Booking>> repositoryResponse =
                bookingRepository.findBookingsByUserResponse(user.getId(), pageable);
        Page<Booking> bookingPage = repositoryResponse.getData();

        if (bookingPage.isEmpty()) {
            return ResponseStructure.success(HttpStatus.OK.value(), "Bookings retrieved successfully",
                    PageResponse.of(bookingPage.map(bookingMapper::toResponse)));
        }

        // Query 2: flights, airports and passengers for those bookings (JOIN FETCH, no N+1).
        List<Long> ids = bookingPage.getContent().stream().map(Booking::getId).toList();
        Map<Long, Booking> detailed = bookingRepository.findAllWithDetailsByIds(ids).stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        return ResponseStructure.success(HttpStatus.OK.value(), "Bookings retrieved successfully",
                PageResponse.of(bookingPage.map(
                        b -> bookingMapper.toResponse(detailed.getOrDefault(b.getId(), b)))));
    }

    @Transactional(readOnly = true)
    public ResponseStructure<BookingResponse> getBookingById(Long bookingId) {
        User user = getLoggedInUser();

        Booking booking = bookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        assertOwner(booking, user);
        return ResponseStructure.success(HttpStatus.OK.value(), "Booking retrieved successfully",
                bookingMapper.toResponse(booking));
    }

    /** Marks the booking CANCELLED and gives the seats back, in one transaction. */
    @Transactional
    public ResponseStructure<BookingResponse> cancelBooking(Long bookingId) {
        User user = getLoggedInUser();

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        assertOwner(booking, user);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking " + bookingId + " is already cancelled");
        }

        int seatsToRestore = booking.getPassengers().size();

        Flight flight = flightRepository.findByIdForUpdate(booking.getFlight().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found for this booking"));

        flight.setAvailableSeats(
                Math.min(flight.getTotalSeats(), flight.getAvailableSeats() + seatsToRestore));
        booking.setStatus(BookingStatus.CANCELLED);

        // Reload with all details for the response (also flushes the changes above).
        return ResponseStructure.success(HttpStatus.OK.value(), "Booking cancelled successfully",
                bookingMapper.toResponse(
                        bookingRepository.findDetailedById(bookingId).orElse(booking)));
    }

    // ------------------------------------------------------------------ helpers

    /** Rule 4: a customer can only touch their own bookings (otherwise 403). */
    private void assertOwner(Booking booking, User user) {
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This booking belongs to another user");
        }
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Logged-in user not found"));
    }
}
