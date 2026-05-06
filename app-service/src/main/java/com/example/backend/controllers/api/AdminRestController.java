package com.example.backend.controllers.api;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.services.OrderService;
import com.example.backend.services.ProductService;
import com.example.backend.services.ReviewService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/reviews/{id}/reply")
    public ResponseEntity<?> replyToReview(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            reviewService.addAdminReply(id, data.get("reply"));
            return ResponseEntity.ok(Map.of("message", "Respuesta añadida correctamente a la reseña."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns dashboard statistics and chart data as JSON.
     * Includes: totals, products by category, sales by category,
     * stock levels, and order status breakdown.
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        List<Product> allProducts = productService.getAllProducts();
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> salesOrders = orderService.getSalesOrders();

        // --- Summary totals ---
        Map<String, Object> totals = new HashMap<>();
        totals.put("totalProducts", allProducts.size());
        totals.put("totalUsers", userService.getAllUsers().size());
        totals.put("totalOrders", allOrders.size());
        totals.put("totalRevenue", salesOrders.stream().mapToDouble(Order::getTotalPrice).sum());

        // --- Products by category (bar chart) ---
        Map<String, Long> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        // --- Sales by category (line/bar chart) ---
        Map<String, Double> salesByCategory = new LinkedHashMap<>();
        for (Order order : salesOrders) {
            if (order.getProducts() != null) {
                for (Product p : order.getProducts()) {
                    salesByCategory.merge(p.getCategory(), p.getPrice(), Double::sum);
                }
            }
        }

        // --- Stock levels (pie chart) ---
        Map<String, Long> stockLevels = new HashMap<>();
        stockLevels.put("inStock", allProducts.stream().filter(p -> p.getStock() > 10).count());
        stockLevels.put("lowStock", allProducts.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 10).count());
        stockLevels.put("outOfStock", allProducts.stream().filter(p -> p.getStock() == 0).count());

        // --- Order status breakdown ---
        Map<String, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        // --- Build response ---
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totals", totals);
        response.put("productsByCategory", productsByCategory);
        response.put("salesByCategory", salesByCategory);
        response.put("stockLevels", stockLevels);
        response.put("ordersByStatus", ordersByStatus);

        return ResponseEntity.ok(response);
    }
}