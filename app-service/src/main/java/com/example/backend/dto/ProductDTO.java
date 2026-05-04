package com.example.backend.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String category;
    private int stock;
    private String brand;
    private boolean active;
    private double averageScore;
    private int reviewCount;
}