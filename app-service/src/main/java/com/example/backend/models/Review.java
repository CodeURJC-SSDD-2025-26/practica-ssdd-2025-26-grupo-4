package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int score; // Rating from 1 to 5
    
    @Column(columnDefinition = "TEXT")
    private String comment; // User's review text
    
    @Column(columnDefinition = "TEXT")
    private String adminReply; // Response from the administrator
    
    private LocalDateTime date;

    // N:1 relationship - Many reviews belong to one product
    @ManyToOne
    private Product product;

    // N:1 relationship - Many reviews are written by one user
    @ManyToOne
    private User user;

    // --- Mustache helpers ---

    /** Returns the date formatted as dd/MM/yyyy for display in templates */
    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Returns CSS width percentage for star rating (score * 20) */
    public int getStarsWidth() {
        return score * 20;
    }

    /** Returns true if the admin has replied to this review */
    public boolean getHasAdminReply() {
        return adminReply != null && !adminReply.isBlank();
    }

    /** Generates filled star icons based on score for Mustache rendering */
    public String getStarsHtml() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= score) {
                sb.append("<i class=\"bi bi-star-fill\"></i>\n");
            } else {
                sb.append("<i class=\"bi bi-star\"></i>\n");
            }
        }
        return sb.toString();
    }
}