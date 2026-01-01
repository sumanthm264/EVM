package com.venue.management.service;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import java.util.List;

public interface SupportTicketService {
    List<SupportTicket> getAllTickets();
    List<SupportTicket> getCustomerTickets(User user);
    SupportTicket createTicket(SupportTicket ticket);
    SupportTicket getTicketById(Long id);
    SupportTicket resolveTicket(Long id);
    long countOpenTickets();
}
