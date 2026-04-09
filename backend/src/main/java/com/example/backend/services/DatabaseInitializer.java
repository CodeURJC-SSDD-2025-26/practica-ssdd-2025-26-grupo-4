package com.example.backend.services;

import com.example.backend.models.*;
import com.example.backend.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class DatabaseInitializer {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostConstruct
    public void init() {
        // 1. Create Users (Admin and Customer)
        User admin = new User();
        admin.setUsername("admin");
        admin.setEncodedPassword("password"); // Will be encoded later with Security
        admin.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(admin);

        User customer = new User();
        customer.setUsername("user");
        customer.setEncodedPassword("password");
        customer.setRoles(Arrays.asList("USER"));
        userRepository.save(customer);

        // 2. Create Products (Sample Components)
        Product p1 = new Product();
        p1.setName("AMD Ryzen 7 9800X3D 4.7/5.2GHz");
        p1.setDescription("Procesador AMD Ryzen 7 9800X3D con tecnología 3D V-Cache de segunda generación. 8 núcleos y 16 hilos. Ideal para gaming de alto rendimiento.");
        p1.setPrice(464.95);
        p1.setStock(24);
        p1.setCategory("Procesadores");
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("NVIDIA RTX 4080 Super");
        p2.setDescription("Experience ultra-performance gaming with ray tracing.");
        p2.setPrice(1099.00);
        p2.setStock(5);
        p2.setCategory("GPU");
        productRepository.save(p2);

        // 3. Create a Review for a product
        Review r1 = new Review();
        r1.setScore(5);
        r1.setComment("Amazing performance, worth every penny!");
        r1.setDate(LocalDateTime.now());
        r1.setProduct(p1);
        r1.setUser(customer);
        reviewRepository.save(r1);
    }
}