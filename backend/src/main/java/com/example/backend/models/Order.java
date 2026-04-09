package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_order") // 'order' is a reserved keyword in SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime orderDate;
    private double totalPrice;
    private String status;         // Example: "Processing", "Shipped", "Delivered"
    private String paymentMethod;  // From your screenshots: Credit Card, PayPal, etc.

    // Simplified Shipping Information (Combined from your form)
    private String shippingAddress;
    private String city;
    private String postalCode;
    private String country;

    // --- RELATIONSHIPS (Requirement 20) ---

    // N:1 relationship - Many orders belong to one user
    @ManyToOne
    private User user;

    // N:M relationship - An order contains many products, and a product can be in many orders
    @ManyToMany
    @JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
}