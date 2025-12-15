package com.shubham.internship_backend.service;

import com.shubham.internship_backend.dto.BookingRequest;
import com.shubham.internship_backend.exception.BadRequestException;
import com.shubham.internship_backend.exception.ConflictException;
import com.shubham.internship_backend.model.Booking;
import com.shubham.internship_backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public List<Booking> getBookings(String hotelId) {
        return bookingRepository.findByHotelIdOrderByStartDateAsc(hotelId);
    }

    public Booking createBooking(String hotelId, String userId, BookingRequest request) {
        log.info("Attempting to create booking for user: {} at hotel: {}", userId, hotelId);

        // 1. Invalid Date Range Conflict
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        // 2. Date Overlap Conflict
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                hotelId,
                request.getStartDate(),
                request.getEndDate());

        if (!conflicts.isEmpty()) {
            log.warn("Booking conflict detected for hotel: {} dates: {} - {}", hotelId, request.getStartDate(),
                    request.getEndDate());
            throw new ConflictException("Booking dates overlap with existing booking");
        }

        // 3. Create and Save Booking
        Booking booking = Booking.builder()
                .hotelId(hotelId)
                .userId(userId)
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(Instant.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        // 4. Notify
        emailService.sendBookingNotification(savedBooking);

        return savedBooking;
    }
}
