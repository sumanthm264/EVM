package com.venue.management.service;

import com.venue.management.entity.Payment;
import com.venue.management.entity.User;

import java.util.List;

public interface PaymentService {
    Payment processPayment(Payment payment);
    Payment getPaymentById(Long id);
    List<Payment> getUserPayments(User user);
    List<Payment> getAllPayments();
    void refundPayment(Long bookingId);
    
    // Admin statistics
    double getTotalEarnings();
    long getSuccessfulPaymentsCount();
    long getPendingPaymentsCount();
    long getRefundedPaymentsCount();
    double getTotalRefundedAmount();
}
