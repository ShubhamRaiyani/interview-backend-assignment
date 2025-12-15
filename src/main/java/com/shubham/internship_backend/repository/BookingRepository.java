package com.shubham.internship_backend.repository;

import com.shubham.internship_backend.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByHotelIdOrderByStartDateAsc(String hotelId);

    @Query("{ 'hotelId': ?0, 'startDate': { $lt: ?2 }, 'endDate': { $gt: ?1 } }")
    List<Booking> findConflictingBookings(
            String hotelId,
            LocalDate startDate,
            LocalDate endDate);
}
