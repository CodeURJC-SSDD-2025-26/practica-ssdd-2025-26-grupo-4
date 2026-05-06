package com.example.backend.services;

import com.example.backend.models.Review;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public Review createReview(String username, Long productId, int score, String comment) throws Exception {
        return userRepository.findByUsername(username)
                .flatMap(user -> productRepository.findById(productId).map(product -> {
                    Review review = new Review();
                    review.setScore(score);
                    review.setComment(comment);
                    review.setDate(LocalDateTime.now());
                    review.setUser(user);
                    review.setProduct(product);
                    return reviewRepository.save(review);
                })).orElseThrow(() -> new Exception("Usuario o producto no encontrado."));
    }

    public void deleteReview(Long reviewId, String username, boolean isAdmin) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Reseña no encontrada."));

        if (isAdmin || (review.getUser() != null && review.getUser().getUsername().equals(username))) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new Exception("Acceso no autorizado para eliminar esta reseña.");
        }
    }

    public void addAdminReply(Long reviewId, String reply) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Reseña no encontrada."));
        review.setAdminReply(reply);
        reviewRepository.save(review);
    }

    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public java.util.List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public java.util.List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReviewAdmin(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review updateReview(Long reviewId, String username, boolean isAdmin, int score, String comment) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Reseña no encontrada."));

        if (isAdmin || (review.getUser() != null && review.getUser().getUsername().equals(username))) {
            review.setScore(score);
            review.setComment(comment);
            return reviewRepository.save(review);
        } else {
            throw new Exception("Acceso no autorizado para editar esta reseña.");
        }
    }
}