package com.venue.management.controller;

import com.venue.management.entity.User;
import com.venue.management.entity.Role;
import com.venue.management.repository.UserRepository;
import com.venue.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

	@Autowired
	private UserService userService;

	@Autowired
	private VenueService venueService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private SupportTicketService supportTicketService;

	@Autowired
	private UserRepository userRepository;
	

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

		return switch (user.getRole()) {
		case ADMIN -> {
			// Add data for admin dashboard
			model.addAttribute("venues", venueService.getAllVenues());
			model.addAttribute("bookings", bookingService.getAllBookings());
			model.addAttribute("openTicketsCount", supportTicketService.countOpenTickets());
			model.addAttribute("pendingApprovals", userRepository.findAll().stream()
					.filter(u -> u.getRole() == Role.EVENT_MANAGER && !u.isEnabled()).toList());
			yield "dashboard/admin";
		}
		case EVENT_MANAGER -> "dashboard/manager";
		default -> "dashboard/index";
		};
	}
}
