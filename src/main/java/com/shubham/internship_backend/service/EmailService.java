package com.shubham.internship_backend.service;

import com.shubham.internship_backend.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${support.email}")
    private String supportEmail;

    @Async
    public void sendBookingNotification(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(supportEmail);
            message.setSubject("New Booking Created");
            message.setText(buildMessage(booking));

            mailSender.send(message);
            log.info("📧 Sent booking notification logic for Booking ID: {}", booking.getId());
        } catch (Exception e) {
            // Do NOT break booking flow
            log.error("Failed to send booking notification email", e);
        }
    }

    private String buildMessage(Booking booking) {
        return """
                New booking created.

                Hotel ID: %s
                Guest Name: %s
                Guest Email: %s
                Booked By (Staff ID): %s
                Dates: %s -> %s
                """.formatted(
                booking.getHotelId(),
                booking.getGuestName(),
                booking.getGuestEmail(),
                booking.getCreatedBy(),
                booking.getStartDate(),
                booking.getEndDate());
    }
}
