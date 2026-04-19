package com.example.backend.services;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.User;
import com.example.backend.repositories.OrderRepository;
import com.example.backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Recommends products based on User-Based Collaborative Filtering.
     * Logic: Find users who bought similar items and suggest products they liked.
     */
    public List<Product> getRecommendedProducts(User currentUser) {
        // 1. Get current user's purchased products
        List<Order> userOrders = orderRepository.findByUserId(currentUser.getId());
        Set<Long> userProductIds = userOrders.stream()
                .flatMap(order -> order.getProducts().stream())
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (userProductIds.isEmpty()) {
            // Fallback: If no history, return top rated or generic products
            return productRepository.findAll().stream().limit(8).collect(Collectors.toList());
        }

        // 2. Find similar users (users who bought at least one of the same products)
        Map<Product, Integer> recommendationsMap = new HashMap<>();
        List<Order> allOrders = orderRepository.findAll();

        for (Order order : allOrders) {
            // Skip the current user's own orders
            if (order.getUser().getId().equals(currentUser.getId())) continue;

            boolean isSimilarUser = order.getProducts().stream()
                    .anyMatch(p -> userProductIds.contains(p.getId()));

            if (isSimilarUser) {
                // 3. Collect products from similar users that the current user hasn't bought
                for (Product p : order.getProducts()) {
                    if (!userProductIds.contains(p.getId())) {
                        recommendationsMap.put(p, recommendationsMap.getOrDefault(p, 0) + 1);
                    }
                }
            }
        }

        // 4. Sort products by frequency (most bought by similar users first)
        return recommendationsMap.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10) // Limit to 10 recommendations
                .collect(Collectors.toList());
    }
}