package com.venue.management.controller;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import com.venue.management.service.BookingService;
import com.venue.management.service.PaymentService;
import com.venue.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/pay/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        // Need to fetch full booking object to show details
        Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst().orElseThrow();
                
        Payment payment = new Payment();
        payment.setBooking(booking);
        
        // Calculate total amount based on number of days
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            booking.getEventDate(), 
            booking.getEndDate()
        ) + 1; // +1 to include both start and end dates
        double totalAmount = booking.getVenue().getPricePerDay() * days;
        payment.setPaymentAmount(totalAmount);
        
        model.addAttribute("payment", payment);
        return "payment/process";
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam("bookingId") Long bookingId, @ModelAttribute Payment payment) {
        // Fetch booking again to ensure consistency
        Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst().orElseThrow();
                
        payment.setBooking(booking);
        paymentService.processPayment(payment);
        return "redirect:/bookings";
    }

    @GetMapping("/my-payments")
    public String myPayments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Payment> payments = paymentService.getUserPayments(user);
        
        // For each payment, determine the display status based on booking status
        for (Payment payment : payments) {
            if ("CANCELLED".equals(payment.getBooking().getStatus())) {
                // If booking is cancelled, ensure payment shows as refunded
                if (!"REFUNDED".equals(payment.getPaymentStatus())) {
                    payment.setPaymentStatus("REFUNDED");
                }
            }
        }
        
        // Calculate summary statistics
        double totalPaid = payments.stream()
            .filter(p -> "SUCCESS".equals(p.getPaymentStatus()) && !"CANCELLED".equals(p.getBooking().getStatus()))
            .mapToDouble(Payment::getPaymentAmount)
            .sum();
        
        long successfulCount = payments.stream()
            .filter(p -> "SUCCESS".equals(p.getPaymentStatus()) && !"CANCELLED".equals(p.getBooking().getStatus()))
            .count();
        
        double refundedAmount = payments.stream()
            .filter(p -> "CANCELLED".equals(p.getBooking().getStatus()))
            .mapToDouble(Payment::getPaymentAmount)
            .sum();
        
        model.addAttribute("payments", payments);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("successfulCount", successfulCount);
        model.addAttribute("refundedAmount", refundedAmount);
        return "payment/my-payments";
    }

    @GetMapping("/admin")
    public String adminPayments(Model model) {
        model.addAttribute("totalEarnings", paymentService.getTotalEarnings());
        model.addAttribute("successfulPaymentsCount", paymentService.getSuccessfulPaymentsCount());
        model.addAttribute("pendingPaymentsCount", paymentService.getPendingPaymentsCount());
        model.addAttribute("refundedPaymentsCount", paymentService.getRefundedPaymentsCount());
        model.addAttribute("totalRefundedAmount", paymentService.getTotalRefundedAmount());
        model.addAttribute("allPayments", paymentService.getAllPayments());
        return "payment/admin-payments";
    }
}
