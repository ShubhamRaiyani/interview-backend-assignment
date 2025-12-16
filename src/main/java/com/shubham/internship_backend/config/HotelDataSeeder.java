package com.shubham.internship_backend.config;

import com.shubham.internship_backend.model.Hotel;
import com.shubham.internship_backend.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotelDataSeeder implements CommandLineRunner {

    private final HotelRepository hotelRepository;

    @Value("${app.seed-data:false}")
    private boolean seedDataEnabled;

    @Override
    public void run(String... args) {

        if (!seedDataEnabled) {
            log.info("⏭️ Data seeding disabled");
            return;
        }

        List<Hotel> hotels = List.of(
                Hotel.builder()
                        .id("HOTEL_001")
                        .name("Taj Palace")
                        .city("Mumbai")
                        .status("ACTIVE")
                        .build(),

                Hotel.builder()
                        .id("HOTEL_002")
                        .name("The Oberoi")
                        .city("Delhi")
                        .status("ACTIVE")
                        .build(),

                Hotel.builder()
                        .id("HOTEL_003")
                        .name("ITC Grand Chola")
                        .city("Chennai")
                        .status("ACTIVE")
                        .build(),

                Hotel.builder()
                        .id("HOTEL_004")
                        .name("Leela Palace")
                        .city("Bengaluru")
                        .status("ACTIVE")
                        .build(),

                Hotel.builder()
                        .id("HOTEL_005")
                        .name("Hyatt Regency")
                        .city("Pune")
                        .status("ACTIVE")
                        .build());

        hotels.forEach(hotel -> {
            if (!hotelRepository.existsById(hotel.getId())) {
                hotelRepository.save(hotel);
                log.info("🏨 Seeded Hotel: {} - {}", hotel.getId(), hotel.getName());
            } else {
                log.info("🏨 Hotel already exists: {}", hotel.getId());
            }
        });

        log.info("✅ Hotel seeding completed");
    }
}