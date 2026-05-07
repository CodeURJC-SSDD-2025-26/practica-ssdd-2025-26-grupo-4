package com.example.backend.controllers.api;

import com.example.backend.dto.ReviewDTO;
import com.example.backend.models.Review;
import com.example.backend.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    private ReviewDTO convertToDTO(Review r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        dto.setScore(r.getScore());
        dto.setComment(r.getComment());
        dto.setDate(r.getDate() != null ? r.getDate().toString() : "");
        dto.setUsername(r.getUser() != null ? r.getUser().getUsername() : "Anonymous");
        dto.setProductId(r.getProduct() != null ? r.getProduct().getId() : null);
        dto.setAdminReply(r.getAdminReply());
        return dto;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> data, Principal principal) {
        try {
            Long productId = Long.valueOf(data.get("productId").toString());
            int score = Integer.parseInt(data.get("score").toString());
            String comment = data.get("comment").toString();

            Review review = reviewService.createReview(principal.getName(), productId, score, comment);
            URI location = URI.create("/api/v1/reviews/" + review.getId());
            return ResponseEntity.created(location).body(convertToDTO(review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Principal principal,
            @RequestHeader(value = "Role", defaultValue = "USER") String role) {
        try {
            boolean isAdmin = role.contains("ADMIN");
            reviewService.deleteReview(id, principal.getName(), isAdmin);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> getAllReviews(Pageable pageable) {
        Page<ReviewDTO> dtos = reviewService.getAllReviews(pageable).map(this::convertToDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        return reviewService.getReviewById(id).map(review -> {
            ReviewDTO dto = convertToDTO(review);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Map<String, Object> data,
            Principal principal, @RequestHeader(value = "Role", defaultValue = "USER") String role) {
        try {
            boolean isAdmin = role.contains("ADMIN");
            int score = Integer.parseInt(data.get("score").toString());
            String comment = data.get("comment").toString();

            Review updatedReview = reviewService.updateReview(id, principal.getName(), isAdmin, score, comment);
            return ResponseEntity.ok(convertToDTO(updatedReview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}