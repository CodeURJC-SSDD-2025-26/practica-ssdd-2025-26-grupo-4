package com.example.backend.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.ProductDTO;
import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.services.OrderService;
import com.example.backend.services.EmailService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

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
        return ResponseEntity.ok(Map.of("message", "El carrito está vacío."));
    }

    @PostMapping("/cart/items/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId, Principal principal) {
        orderService.addProductToUserCart(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Producto " + productId + " añadido al carrito correctamente."));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, Principal principal) {
        orderService.removeProductFromUserCart(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Producto " + productId + " eliminado del carrito."));
    }

    @PutMapping("/cart/update/{productId}")
    public ResponseEntity<?> updateCartQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            Principal principal) {
        orderService.updateProductQuantityInUserCart(principal.getName(), productId, quantity);
        return ResponseEntity.ok(Map.of("message", "Cantidad de producto actualizada correctamente."));
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

            URI location = URI.create("/api/v1/orders/" + completedOrder.getId());
            return ResponseEntity.created(location).body(convertToDTO(completedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> orderData) {
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new Exception("Pedido no encontrado"));

            order.setStatus(orderData.get("status").toString());

            Order updatedOrder = orderService.saveOrder(order);
            return ResponseEntity.ok(convertToDTO(updatedOrder));
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

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDTO>> getAllOrdersAdmin(Pageable pageable) {
        Page<OrderDTO> orderDTOs = orderService.getAllOrders(pageable)
                .map(this::convertToDTO);

        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id).map(order -> {
            OrderDTO dto = convertToDTO(order);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resend-invoice")
    public ResponseEntity<?> resendInvoiceEmail(@PathVariable Long id, Principal principal) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(id);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Verify ownership (user can only resend their own invoice)
            if (!order.getUser().getUsername().equals(principal.getName()) && 
                !principal.getName().equals("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permiso para resend este email"));
            }

            // Send invoice email with explicit error handling
            emailService.sendInvoiceEmailInternal(order);
            return ResponseEntity.ok(Map.of("message", "Correo de factura reenviado exitosamente a " + order.getUser().getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar el correo: " + e.getMessage()));
        }
    }
}