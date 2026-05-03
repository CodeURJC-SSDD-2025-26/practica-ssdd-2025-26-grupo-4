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
import com.example.backend.services.ProductService;
import com.example.backend.services.ReviewService;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

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
            reviewService.saveReview(productId, principal.getName(), score, comment);
        }
        return "redirect:/item-detail?id=" + productId;
    }

    @PostMapping("/review/delete")
    public String deleteReviewByUser(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Obtenemos el ID del producto antes de borrar para la redirección
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        Long productId = reviewOpt.map(r -> r.getProduct().getId()).orElse(null);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean deleted = reviewService.deleteReviewWithCheck(id, principal.getName(), isAdmin);

        if (deleted) {
            return productId != null ? "redirect:/item-detail?id=" + productId : "redirect:/";
        }
        return "redirect:/error/403";
    }
}