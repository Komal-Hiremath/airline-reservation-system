package org.qsp.airportmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.qsp.airportmanagement.dto.BookingRequest;
import org.qsp.airportmanagement.dto.BookingResponse;
import org.qsp.airportmanagement.dto.PageResponse;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a booking with passengers (customer)")
    @PostMapping
    public ResponseEntity<ResponseStructure<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        ResponseStructure<BookingResponse> response = bookingService.createBooking(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "My bookings (customer, paged)",
            description = "sort format: field,direction  e.g. createdAt,desc")
    @GetMapping
    public ResponseEntity<ResponseStructure<PageResponse<BookingResponse>>> myBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        ResponseStructure<PageResponse<BookingResponse>> response =
                bookingService.getMyBookings(page, size, sort);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "One of my bookings (customer)")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseStructure<BookingResponse>> getBooking(@PathVariable Long id) {
        ResponseStructure<BookingResponse> response = bookingService.getBookingById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Cancel my booking (customer)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ResponseStructure<BookingResponse>> cancelBooking(@PathVariable Long id) {
        ResponseStructure<BookingResponse> response = bookingService.cancelBooking(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
