package com.example.backend.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import com.example.backend.models.Product;
import com.example.backend.models.User;
import com.example.backend.services.ProductService;
import com.example.backend.services.UserService;
import com.example.backend.services.RecommendationService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        // Get the principal (logged user)
        Principal principal = request.getUserPrincipal();
        boolean isLoggedIn = principal != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 1. Hardware News Section
        // Your HTML expects 'productos' for this section
        List<Product> hardwareNews = productService.getLatestProducts(8);
        model.addAttribute("productos", hardwareNews);

        // 2. Recommendations Section ("You might be interested in")
        if (isLoggedIn) {
            Optional<User> userOpt = userService.findByUsername(principal.getName());
            if (userOpt.isPresent()) {
                List<Product> recommendations = recommendationService.getRecommendedProducts(userOpt.get());
                model.addAttribute("recomendados", recommendations);
                // Useful for showing/hiding the section if empty
                model.addAttribute("hasRecommendations", !recommendations.isEmpty());
            }
        } else {
            // Fallback for guests: show the same hardware news or a random selection
            model.addAttribute("recomendados", hardwareNews);
            model.addAttribute("hasRecommendations", true);
        }

        // CSRF Token (needed for forms in the index if any)
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            model.addAttribute("_csrf", token);
        }

        return "index";
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("productos", productService.getAllProducts());
        return "index";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error-403";
    }
}
