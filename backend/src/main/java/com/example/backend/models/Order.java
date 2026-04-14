package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // --- Mustache helpers ---

    /** Returns the order date formatted as dd/MM/yyyy for display */
    public String getFormattedDate() {
        if (orderDate == null) return "";
        return orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Returns the CSS badge class based on the order status */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary";
        switch (status.toUpperCase()) {
            case "ENTREGADO": return "bg-success";
            case "ENVIADO": return "bg-primary";
            case "EN PROCESO": return "bg-warning text-dark";
            case "PENDIENTE": return "bg-info text-dark";
            case "CANCELADO": return "bg-danger";
            default: return "bg-secondary";
        }
    }

    /** Returns the formatted total price with comma as decimal separator */
    public String getFormattedPrice() {
        return String.format("%.2f", totalPrice).replace('.', ',');
    }
}