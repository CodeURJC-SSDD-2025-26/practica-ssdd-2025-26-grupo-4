package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String username;

    private String email;
    private String encodedPassword; // Needed for Spring Security
    private String firstName;
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>(); // Example: ["ROLE_USER ", "ROLE_ADMIN"]

    @Lob
    private Blob profilePicture; // Requirement: store images in DB
    private boolean hasPicture;

    // --- RELATIONSHIPS (Requirement 20) ---

    // 1:N relationship - One user can have multiple orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    // 1:N relationship - One user can write multiple reviews
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;

    // --- Mustache helpers ---

    /** Returns true if this user has the ROLE_ADMIN role */
    public boolean getIsAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    /** Returns the display name: firstName + lastName if available, otherwise username */
    public String getDisplayName() {
        if (firstName != null && !firstName.isBlank()) {
            String name = firstName;
            if (lastName != null && !lastName.isBlank()) {
                name += " " + lastName;
            }
            return name;
        }
        return username;
    }

    /** Returns the role label for display (Admin / Cliente) */
    public String getRoleLabel() {
        return getIsAdmin() ? "Admin" : "Cliente";
    }

    /** Returns the CSS badge class for the role */
    public String getRoleBadgeClass() {
        return getIsAdmin() ? "bg-danger" : "bg-info text-dark";
    }
}