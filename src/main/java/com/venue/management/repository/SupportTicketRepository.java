package com.venue.management.repository;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCustomer(User customer);
    long countByTicketStatus(String ticketStatus);
}
