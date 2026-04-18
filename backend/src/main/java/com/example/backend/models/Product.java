package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;
    private String category;
    private int stock;
    private String brand;
    @Lob
    private Blob imageFile;

    private boolean image;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;

    public double getAverageScore() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Review review : reviews) {
            sum += review.getScore();
        }
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    public int getReviewCount() {
        return (reviews != null) ? reviews.size() : 0;
    }

    public double getAverageStarsWidth() {
        return getAverageScore() * 20;
    }
}