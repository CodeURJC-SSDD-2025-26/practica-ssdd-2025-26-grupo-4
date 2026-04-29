package com.example.backend.controllers;
import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.backend.models.Review;
import com.example.backend.models.User;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;

@Controller
public class ReviewController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/create-review")
    public String createReview(@RequestParam(value = "productId", required = false) Long productId, Model model) {
        if (productId != null) {
            productRepository.findById(productId).ifPresent(product -> {
                model.addAttribute("producto", product);
            });
        }
        model.addAttribute("productos", productRepository.findAll());
        return "pages/create-review";
    }

    @PostMapping("/create-review")
    public String submitReview(@RequestParam Long productId,
            @RequestParam int score,
            @RequestParam String comment,
            Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                productRepository.findById(productId).ifPresent(product -> {
                    Review review = new Review();
                    review.setScore(score);
                    review.setComment(comment);
                    review.setDate(java.time.LocalDateTime.now());
                    review.setUser(user);
                    review.setProduct(product);
                    reviewRepository.save(review);
                });
            });
        }
        return "redirect:/item-detail?id=" + productId;
    }
    
    
    // Allow users (owner) or admins to delete their own reviews
    @PostMapping("/review/delete")
    public String deleteReviewByUser(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        Optional<Review> optReview = reviewRepository.findById(id);
        if (optReview.isEmpty()) return "redirect:/";
        Review review = optReview.get();

        Optional<User> currentUserOpt = userRepository.findByUsername(principal.getName());
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin || (review.getUser() != null && currentUserOpt.isPresent()
                && review.getUser().getId().equals(currentUserOpt.get().getId()))) {
            Long productId = review.getProduct() != null ? review.getProduct().getId() : null;
            reviewRepository.deleteById(id);
            return productId != null ? "redirect:/item-detail?id=" + productId : "redirect:/";
        }
        return "redirect:/error/403";
    }
}
