package com.example.backend.services;

import com.example.backend.models.Review;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public Review createReview(String username, Long productId, int score, String comment) throws Exception {
        return userRepository.findByUsername(username).flatMap(user -> 
            productRepository.findById(productId).map(product -> {
                Review review = new Review();
                review.setScore(score);
                review.setComment(comment);
                review.setDate(LocalDateTime.now());
                review.setUser(user);
                review.setProduct(product);
                return reviewRepository.save(review);
            })
        ).orElseThrow(() -> new Exception("User or product not found."));
    }

    public void deleteReview(Long reviewId, String username, boolean isAdmin) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found."));

        if (isAdmin || (review.getUser() != null && review.getUser().getUsername().equals(username))) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new Exception("Unauthorized access to delete this review.");
        }
    }

    public void addAdminReply(Long reviewId, String reply) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found."));
        review.setAdminReply(reply);
        reviewRepository.save(review);
    }
}