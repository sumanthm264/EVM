package com.venue.management.service.impl;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import com.venue.management.repository.SupportTicketRepository;
import com.venue.management.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Override
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    @Override
    public List<SupportTicket> getCustomerTickets(User user) {
        return supportTicketRepository.findByCustomer(user);
    }

    @Override
    public SupportTicket createTicket(SupportTicket ticket) {
        ticket.setTicketStatus("OPEN");
        ticket.setCreatedDate(LocalDateTime.now());
        return supportTicketRepository.save(ticket);
    }

    @Override
    public SupportTicket getTicketById(Long id) {
        return supportTicketRepository.findById(id).orElseThrow();
    }

    @Override
    public SupportTicket resolveTicket(Long id, String resolutionNotes) {
        SupportTicket ticket = supportTicketRepository.findById(id).orElseThrow();
        ticket.setTicketStatus("RESOLVED");
        ticket.setResolvedDate(LocalDateTime.now());
        ticket.setResolutionNotes(resolutionNotes);
        return supportTicketRepository.save(ticket);
    }
    
    @Override
    public long countOpenTickets() {
        return supportTicketRepository.countByTicketStatus("OPEN");
    }
}
