package com.venue.management.repository;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    List<Payment> findByBooking_User_UserId(Long userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = ?1")
    long countByPaymentStatus(String status);
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    Double sumSuccessfulPayments();
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.paymentStatus = 'REFUNDED'")
    Double sumRefundedPayments();
}
