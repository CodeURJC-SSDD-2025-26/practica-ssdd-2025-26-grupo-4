package com.example.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.backend.models.Product;
import com.example.backend.models.User;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.UserRepository;

@Service
public class MainService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationService recommendationService;

    // ================= HOME DATA =================

    public List<Product> getLatestProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .limit(8)
                .collect(Collectors.toList());
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getRecommendations(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            return recommendationService.getRecommendedProducts(userOpt.get());
        }

        return List.of();
    }

    public boolean hasRecommendations(List<Product> recommendations) {
        return recommendations != null && !recommendations.isEmpty();
    }
}