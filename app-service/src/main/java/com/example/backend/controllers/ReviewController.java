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
import com.example.backend.services.ProductService;
import com.example.backend.services.ReviewService;
import com.example.backend.services.UserService;

@Controller
public class ReviewController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/create-review")
    public String createReview(@RequestParam(value = "productId", required = false) Long productId, Model model) {
        if (productId != null) {
            productService.getProductById(productId).ifPresent(product -> {
                model.addAttribute("producto", product);
            });
        }
        model.addAttribute("productos", productService.getAllProducts());
        return "pages/create-review";
    }

    @PostMapping("/create-review")
    public String submitReview(@RequestParam Long productId,
            @RequestParam int score,
            @RequestParam String comment,
            Principal principal) {
        if (principal != null) {
            try {
                reviewService.createReview(principal.getName(), productId, score, comment);
            } catch (Exception e) {
                // Review creation failed — redirect back to the product page silently
            }
        }
        return "redirect:/item-detail?id=" + productId;
    }
    
    
    // Allow users (owner) or admins to delete their own reviews
    @PostMapping("/review/delete")
    public String deleteReviewByUser(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        Optional<Review> optReview = reviewService.getReviewById(id);
        if (optReview.isEmpty()) return "redirect:/";
        Review review = optReview.get();
        Long productId = review.getProduct() != null ? review.getProduct().getId() : null;

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            reviewService.deleteReview(id, principal.getName(), isAdmin);
            return productId != null ? "redirect:/item-detail?id=" + productId : "redirect:/";
        } catch (Exception e) {
            return "redirect:/error/403";
        }
    }
}
