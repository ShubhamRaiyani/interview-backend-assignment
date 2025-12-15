package com.shubham.internship_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "hotel_date_idx", def = "{'hotelId': 1, 'startDate': 1, 'endDate': 1}")
public class Booking {

    @Id
    private String id;

    private String hotelId;

    private String createdBy; // staff_user_id (Supabase sub)

    // 👇 Guest (customer) details
    private String guestName;
    private String guestEmail;

    private LocalDate startDate;

    private LocalDate endDate;

    private Instant createdAt;
}
