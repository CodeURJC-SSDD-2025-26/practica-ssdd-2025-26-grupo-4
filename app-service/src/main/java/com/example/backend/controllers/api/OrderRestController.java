package com.example.backend.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.ProductDTO;
import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.services.OrderService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    private ProductDTO convertProductToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setCategory(p.getCategory());
        return dto;
    }

    private OrderDTO convertToDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId());
        dto.setOrderDate(o.getOrderDate() != null ? o.getOrderDate().toString() : "N/A");
        dto.setTotalPrice(o.getTotalPrice());
        dto.setStatus(o.getStatus());
        dto.setPaymentMethod(o.getPaymentMethod());
        dto.setShippingAddress(o.getShippingAddress());
        dto.setCity(o.getCity());
        dto.setPostalCode(o.getPostalCode());
        dto.setCountry(o.getCountry());

        if (o.getUser() != null) {
            dto.setUsername(o.getUser().getUsername());
        }

        if (o.getProducts() != null) {
            List<ProductDTO> productDTOs = o.getProducts().stream()
                    .map(this::convertProductToDTO)
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);
        }

        return dto;
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getActiveCart(Principal principal) {
        Optional<Order> orderOpt = orderService.getActiveOrderForUser(principal.getName());
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(convertToDTO(orderOpt.get()));
        }
        return ResponseEntity.ok(Map.of("message", "The cart is empty."));
    }

    @PostMapping("/cart/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId, Principal principal) {
        orderService.addProductToUserCart(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Product " + productId + " successfully added to cart."));
    }

    @DeleteMapping("/cart/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, Principal principal) {
        orderService.removeProductFromUserCart(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Product " + productId + " removed from cart."));
    }

    @PutMapping("/cart/update/{productId}")
    public ResponseEntity<?> updateCartQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            Principal principal) {
        orderService.updateProductQuantityInUserCart(principal.getName(), productId, quantity);
        return ResponseEntity.ok(Map.of("message", "Product quantity updated successfully."));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(
            @RequestBody Map<String, Object> paymentData,
            Principal principal) {
        try {
            Long addressId = paymentData.get("addressId") != null
                    ? Long.valueOf(paymentData.get("addressId").toString())
                    : null;
            String cardName = (String) paymentData.get("cardName");
            String cardNumber = (String) paymentData.get("cardNumber");

            Order completedOrder = orderService.processPayment(principal.getName(), addressId, cardName, cardNumber);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(completedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Principal principal, Pageable pageable) {
        Page<OrderDTO> orderDTOs = orderService.getOrdersByUserUsername(principal.getName(), pageable)
                .map(this::convertToDTO);

        return ResponseEntity.ok(orderDTOs);
    }
}