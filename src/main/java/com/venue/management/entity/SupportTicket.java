package com.venue.management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(columnDefinition = "TEXT")
    private String issueDescription;

    private String ticketStatus; // OPEN, RESOLVED

    private LocalDateTime createdDate;

    private LocalDateTime resolvedDate;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    public SupportTicket() {
    }

    public SupportTicket(Long ticketId, User customer, String issueDescription, String ticketStatus,
            LocalDateTime createdDate, LocalDateTime resolvedDate, String resolutionNotes) {
        this.ticketId = ticketId;
        this.customer = customer;
        this.issueDescription = issueDescription;
        this.ticketStatus = ticketStatus;
        this.createdDate = createdDate;
        this.resolvedDate = resolvedDate;
        this.resolutionNotes = resolutionNotes;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
