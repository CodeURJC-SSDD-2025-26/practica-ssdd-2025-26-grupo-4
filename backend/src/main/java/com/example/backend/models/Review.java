package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
}