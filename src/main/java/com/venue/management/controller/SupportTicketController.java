package com.venue.management.controller;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import com.venue.management.service.SupportTicketService;
import com.venue.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/support")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listTickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
            model.addAttribute("tickets", supportTicketService.getAllTickets());
        } else {
            model.addAttribute("tickets", supportTicketService.getCustomerTickets(user));
        }
        return "support/list";
    }

    @GetMapping("/create")
    public String createTicketPage(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        return "support/create";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute SupportTicket ticket, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        ticket.setCustomer(user);
        supportTicketService.createTicket(ticket);
        return "redirect:/support";
    }

    @GetMapping("/resolve/{id}")
    public String resolveTicketPage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        SupportTicket ticket = supportTicketService.getTicketById(id);
        
        // Security check: If ticket was created by a manager, only admin can resolve it
        if (ticket.getCustomer().getRole().name().equals("EVENT_MANAGER") 
            && !user.getRole().name().equals("ADMIN")) {
            // Manager trying to resolve a manager-created ticket - redirect with error
            redirectAttributes.addFlashAttribute("error", "You are not authorized to resolve tickets created by managers.");
            return "redirect:/support";
        }
        
        // Check if ticket is already resolved
        if ("RESOLVED".equals(ticket.getTicketStatus())) {
            redirectAttributes.addFlashAttribute("error", "This ticket is already resolved.");
            return "redirect:/support";
        }
        
        model.addAttribute("ticket", ticket);
        return "support/resolve";
    }

    @PostMapping("/resolve/{id}")
    public String resolveTicket(@PathVariable Long id, 
                               @RequestParam("resolutionNotes") String resolutionNotes,
                               @AuthenticationPrincipal UserDetails userDetails, 
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        SupportTicket ticket = supportTicketService.getTicketById(id);
        
        // Security check: If ticket was created by a manager, only admin can resolve it
        if (ticket.getCustomer().getRole().name().equals("EVENT_MANAGER") 
            && !user.getRole().name().equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to resolve tickets created by managers.");
            return "redirect:/support";
        }
        
        // Validate resolution notes
        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Resolution notes are required.");
            return "redirect:/support/resolve/" + id;
        }
        
        supportTicketService.resolveTicket(id, resolutionNotes.trim());
        redirectAttributes.addFlashAttribute("success", "Ticket resolved successfully.");
        return "redirect:/support";
    }
}
