package com.example.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.backend.models.Product;
import com.example.backend.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/item-detail")
    public String itemDetail(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            productService.getProductById(id).ifPresent(product -> {
                model.addAttribute("producto", product);
                model.addAttribute("reviews", productService.getReviewsByProductId(id));
            });
        }
        return "pages/item-detail";
    }

    @GetMapping("/search-result")
    public String searchResult(@RequestParam(value = "category", required = false) String category, Model model) {
        model.addAttribute("productos", productService.getProductsByCategory(category));
        if (category != null && !category.isBlank()) {
            model.addAttribute("categoryName", category);
        }
        return "pages/search-result";
    }

    @GetMapping("/search")
    public String searchProducts(Model model,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            HttpServletRequest request) {

        try {
            // Delegate filtering and sorting to the service layer
            List<Product> results = productService.advancedSearch(name, category, brand, minPrice, maxPrice, sort);

            model.addAttribute("productos", results);
            model.addAttribute("query", name != null ? name : "");
            model.addAttribute("category", category != null ? category : "");
            model.addAttribute("brand", brand != null ? brand : "");
            model.addAttribute("currentSort", sort != null ? sort : "");
            model.addAttribute("searchTerm", (name != null && !name.trim().isEmpty()) ? name : null);

            model.addAttribute("isLoggedIn", request.getUserPrincipal() != null);
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
            if (token != null) {
                model.addAttribute("_csrf", token);
            }

            return "pages/search-result";
        } catch (Exception e) {
            return "redirect:/?error=true";
        }
    }
}