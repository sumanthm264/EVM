package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> getCustomerBookings(User user) {
        return bookingRepository.findByUser(user);
    }

    @Override
    public Booking createBooking(Booking booking) {
        // Check for date range conflicts
        List<Booking> allBookings = bookingRepository.findAll();
        boolean conflict = allBookings.stream()
                .anyMatch(b -> b.getVenue().getVenueId().equals(booking.getVenue().getVenueId()) &&
                               !"CANCELLED".equals(b.getStatus()) &&
                               isDateRangeOverlapping(
                                   booking.getEventDate(), 
                                   booking.getEndDate(),
                                   b.getEventDate(),
                                   b.getEndDate() != null ? b.getEndDate() : b.getEventDate()
                               ));
        
        if (conflict) {
            throw new RuntimeException("Venue is already booked for the selected date range.");
        }
        booking.setStatus("PENDING");
        return bookingRepository.save(booking);
    }

    private boolean isDateRangeOverlapping(java.time.LocalDate start1, java.time.LocalDate end1,
                                          java.time.LocalDate start2, java.time.LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    @Override
    public Booking updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}
