package com.venue.management.controller;

import com.venue.management.entity.Venue;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public String listVenues(Model model) {
        model.addAttribute("venues", venueService.getAllVenues());
        return "venue/list";
    }

    @GetMapping("/add")
    public String addVenuePage(Model model) {
        model.addAttribute("venue", new Venue());
        return "venue/add";
    }

    @PostMapping("/add")
    public String addVenue(@ModelAttribute Venue venue,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          RedirectAttributes redirectAttributes) {
        try {
            if (venue.getVenueId() == null) {
                venue.setStatus("AVAILABLE");
            } else {
                // When editing, preserve the existing status if not set
                if (venue.getStatus() == null || venue.getStatus().isEmpty()) {
                    Venue existingVenue = venueService.getVenueById(venue.getVenueId())
                        .orElseThrow(() -> new RuntimeException("Venue not found"));
                    venue.setStatus(existingVenue.getStatus());
                }
            }

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = saveImage(imageFile);
                venue.setImagePath(imagePath);
            } else if (venue.getVenueId() != null) {
                // If editing and no new image, keep existing image path
                Venue existingVenue = venueService.getVenueById(venue.getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
                venue.setImagePath(existingVenue.getImagePath());
            }

            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue saved successfully!");
            return "redirect:/venues";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving venue: " + e.getMessage());
            return "redirect:/venues/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editVenuePage(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
        model.addAttribute("venue", venue);
        return "venue/add"; // Reuse the add form
    }

    @GetMapping("/maintenance/{id}")
    public String markAsMaintenance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Venue venue = venueService.getVenueById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
            venue.setStatus("MAINTENANCE");
            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue marked as maintenance successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating venue status: " + e.getMessage());
        }
        return "redirect:/venues/edit/" + id;
    }

    @GetMapping("/available/{id}")
    public String markAsAvailable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Venue venue = venueService.getVenueById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
            venue.setStatus("AVAILABLE");
            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue marked as available successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating venue status: " + e.getMessage());
        }
        return "redirect:/venues/edit/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Venue venue = venueService.getVenueById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
            
            // Delete image file if exists
            if (venue.getImagePath() != null && !venue.getImagePath().isEmpty()) {
                deleteImage(venue.getImagePath());
            }
            
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("success", "Venue deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting venue: " + e.getMessage());
        }
        return "redirect:/venues";
    }

    private String saveImage(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get("src/main/resources/static/images");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for database storage
        return "/images/" + filename;
    }

    private void deleteImage(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Path filePath = Paths.get("src/main/resources/static" + imagePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't fail the delete operation
            System.err.println("Error deleting image file: " + e.getMessage());
        }
    }
}
