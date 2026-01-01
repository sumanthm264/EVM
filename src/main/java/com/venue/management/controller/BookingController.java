package com.venue.management.controller;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue;
import com.venue.management.service.BookingService;
import com.venue.management.service.UserService;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        java.util.List<Booking> bookings;
        // Admins see all, customers see theirs
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
            bookings = bookingService.getAllBookings();
        } else {
            bookings = bookingService.getCustomerBookings(user);
        }
        
        // Auto-mark bookings as COMPLETED if current date passes end date
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        for (Booking booking : bookings) {
            if (!"COMPLETED".equals(booking.getStatus()) && 
                !"CANCELLED".equals(booking.getStatus()) &&
                booking.getEndDate() != null &&
                currentDate.isAfter(booking.getEndDate())) {
                bookingService.updateStatus(booking.getBookingId(), "COMPLETED");
            }
        }
        
        // Refresh bookings after status updates
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
            bookings = bookingService.getAllBookings();
        } else {
            bookings = bookingService.getCustomerBookings(user);
        }
        
        model.addAttribute("bookings", bookings);
        return "booking/list";
    }

    @GetMapping("/create/{venueId}")
    public String createBookingPage(@PathVariable Long venueId, Model model) {
        Venue venue = venueService.getVenueById(venueId).orElseThrow();
        Booking booking = new Booking();
        booking.setVenue(venue);
        model.addAttribute("booking", booking);
        return "booking/create";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam("venueId") Long venueId, 
                               @ModelAttribute Booking booking, 
                               @AuthenticationPrincipal UserDetails userDetails, 
                               Model model) {
        try {
            Venue venue = venueService.getVenueById(venueId).orElseThrow();
            booking.setVenue(venue);
            
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            booking.setUser(user);
            
            // Set start date to current date if not provided
            if (booking.getEventDate() == null) {
                booking.setEventDate(java.time.LocalDate.now());
            }
            
            // Validate end date is after start date
            if (booking.getEndDate() == null || booking.getEndDate().isBefore(booking.getEventDate())) {
                booking.setEndDate(booking.getEventDate());
            }
            
            bookingService.createBooking(booking);
            return "redirect:/bookings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", booking);
            model.addAttribute("venue", venueService.getVenueById(venueId).orElseThrow());
            return "booking/create";
        }
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.updateStatus(id, "CANCELLED");
        return "redirect:/bookings";
    }

    @GetMapping("/complete/{id}")
    public String completeBooking(@PathVariable Long id) {
        bookingService.updateStatus(id, "COMPLETED");
        return "redirect:/bookings";
    }
}
