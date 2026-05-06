package com.example.backend.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.services.EmailService;
import com.example.backend.services.OrderService;
import com.example.backend.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model, Principal principal, HttpSession session) {
        List<Map<String, Object>> items;

        if (principal != null) {
            Optional<Order> currentOrder = orderService.getActiveOrderForUser(principal.getName());
            if (currentOrder.isPresent()) {
                Order order = currentOrder.get();
                model.addAttribute("pedido", order);
                items = orderService.buildCartItems(order.getProducts());
            } else {
                items = new ArrayList<>();
            }
        } else {
            List<Product> sessionCart = (List<Product>) session.getAttribute("cart");
            if (sessionCart == null) sessionCart = new ArrayList<>();
            items = orderService.buildCartItems(sessionCart);
        }

        double realTotal = items.stream().mapToDouble(i -> (Double) i.get("lineTotal")).sum();
        
        model.addAttribute("productosCarrito", items);
        model.addAttribute("hasProductosCarrito", !items.isEmpty());
        model.addAttribute("precioTotal", String.format("%.2f", realTotal).replace('.', ','));

        List<Product> allProducts = productService.getAllProducts();
        model.addAttribute("recomendados", allProducts.size() > 4 ? allProducts.subList(0, 4) : allProducts);

        return "pages/shopping-cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, Principal principal, HttpSession session) {
        if (principal != null) {
            orderService.addProductToUserCart(principal.getName(), productId);
        } else {
            List<Product> cart = (List<Product>) session.getAttribute("cart");
            if (cart == null) cart = new ArrayList<>();
            
            final List<Product> sessionCart = cart; 
            productService.getProductById(productId).ifPresent(p -> {
                sessionCart.add(p);
                session.setAttribute("cart", sessionCart);
            });
        }
        return "redirect:/shopping-cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, Principal principal, HttpSession session) {
        if (principal != null) {
            orderService.removeProductFromUserCart(principal.getName(), productId);
        } else {
            List<Product> sessionCart = (List<Product>) session.getAttribute("cart");
            if (sessionCart != null) {
                for (int i = 0; i < sessionCart.size(); i++) {
                    if (sessionCart.get(i).getId().equals(productId)) {
                        sessionCart.remove(i);
                        break;
                    }
                }
                session.setAttribute("cart", sessionCart);
            }
        }
        return "redirect:/shopping-cart";
    }

    @PostMapping("/cart/update")
    public String updateCartQuantity(@RequestParam Long productId, @RequestParam int quantity, Principal principal, HttpSession session) {
        if (principal != null) {
            orderService.updateProductQuantityInUserCart(principal.getName(), productId, quantity);
        } else {
            List<Product> cart = (List<Product>) session.getAttribute("cart");
            if (cart == null) cart = new ArrayList<>();
            
            cart.removeIf(p -> p.getId().equals(productId));
            
            final List<Product> sessionCart = cart; 
            final int qty = Math.max(0, quantity);

            productService.getProductById(productId).ifPresent(p -> {
                for (int i = 0; i < qty; i++) {
                    sessionCart.add(p);
                }
                session.setAttribute("cart", sessionCart);
            });
        }
        return "redirect:/shopping-cart";
    }

    @GetMapping("/payment")
    public String payment(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            model.addAttribute("direcciones", orderService.getUserAddresses(principal.getName()));
            Optional<Order> currentOrder = orderService.getActiveOrderForUser(principal.getName());
            
            if (currentOrder.isPresent()) {
                List<Map<String, Object>> items = orderService.buildCartItems(currentOrder.get().getProducts());
                model.addAttribute("productosCarrito", items);
                double realTotal = items.stream().mapToDouble(i -> (Double) i.get("lineTotal")).sum();
                model.addAttribute("precioTotal", String.format("%.2f", realTotal).replace('.', ','));
            } else {
                model.addAttribute("productosCarrito", new ArrayList<>());
                model.addAttribute("precioTotal", "0,00");
            }
        } else {
            model.addAttribute("productosCarrito", new ArrayList<>());
            model.addAttribute("precioTotal", "0,00");
        }

        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) model.addAttribute("_csrf", token);
        
        return "pages/payment";
    }

    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam(required = false) Long shipAddressId,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String cardNumber,
            Principal principal) {
        try {
            Order order = orderService.processPayment(principal.getName(), shipAddressId, cardName, cardNumber);
            return "redirect:/payment-correct?orderId=" + order.getId();
        } catch (Exception e) {
            if ("Dirección no válida".equals(e.getMessage())) return "redirect:/error/403";
            return "redirect:/shopping-cart?error=" + ("No hay un pedido activo".equals(e.getMessage()) ? "noorder" : "payment");
        }
    }

    @GetMapping("/payment-correct")
    public String showSuccessPage(@RequestParam(required = false) Long orderId, Model model) {
        if (orderId != null) {
            orderService.getOrderById(orderId).ifPresent(order -> {
                model.addAttribute("order", order);
                model.addAttribute("orderId", orderId);
                model.addAttribute("totalPrice", String.format("%.2f", order.getTotalPrice()).replace('.', ','));
                model.addAttribute("productCount", order.getProducts() != null ? order.getProducts().size() : 0);
            });
        }
        return "pages/payment-correct";
    }

    @PostMapping("/address/add")
    public String addAddress(@RequestParam String street, @RequestParam String city,
            @RequestParam String postalCode, @RequestParam String country, Principal principal) {
        if (principal != null) {
            orderService.addUserAddress(principal.getName(), street, city, postalCode, country);
        }
        return "redirect:/payment";
    }

    @GetMapping("/download-invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id, Principal principal) {
        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (!order.getUser().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(403).build();
            }
            try {
                byte[] pdfBytes = emailService.generatePdfInvoice(order);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura_" + id + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfBytes);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}