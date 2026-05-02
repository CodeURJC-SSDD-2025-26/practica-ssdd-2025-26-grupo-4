package com.example.backend.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    // TODO: @Autowired private ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> reviewData, Principal principal) {
        // TODO: Crear la reseña asociándola al producto y al usuario logueado
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Reseña publicada, kuso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Principal principal) {
        // TODO: Comprobar si el usuario es el dueño de la reseña o si es ADMIN, y borrarla
        return ResponseEntity.noContent().build();
    }
}