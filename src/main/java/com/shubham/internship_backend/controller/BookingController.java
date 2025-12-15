package com.shubham.internship_backend.controller;

import com.shubham.internship_backend.dto.BookingRequest;
import com.shubham.internship_backend.model.Booking;
import com.shubham.internship_backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels/{hotelId}/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getBookings(@PathVariable String hotelId) {
        return ResponseEntity.ok(bookingService.getBookings(hotelId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'RECEPTION')")
    public ResponseEntity<Booking> createBooking(
            @PathVariable String hotelId,
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // Use sub from Supabase JWT as userId
        log.info("Received booking request for hotel: {} from user: {}", hotelId, userId);

        Booking booking = bookingService.createBooking(hotelId, userId, request);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }
}
