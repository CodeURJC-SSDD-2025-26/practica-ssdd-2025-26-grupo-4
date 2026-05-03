package com.example.backend.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.models.Product;
import com.example.backend.models.Review;
import com.example.backend.models.User;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public void saveReview(Long productId, String username, int score, String comment) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isPresent() && productOpt.isPresent()) {
            Review review = new Review();
            review.setScore(score);
            review.setComment(comment);
            review.setDate(LocalDateTime.now());
            review.setUser(userOpt.get());
            review.setProduct(productOpt.get());
            reviewRepository.save(review);
        }
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public boolean deleteReviewWithCheck(Long id, String username, boolean isAdmin) {
        Optional<Review> optReview = reviewRepository.findById(id);
        if (optReview.isEmpty()) return false;

        Review review = optReview.get();
        // Lógica de seguridad: solo el dueño o un admin pueden borrar
        if (isAdmin || (review.getUser() != null && review.getUser().getUsername().equals(username))) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
}