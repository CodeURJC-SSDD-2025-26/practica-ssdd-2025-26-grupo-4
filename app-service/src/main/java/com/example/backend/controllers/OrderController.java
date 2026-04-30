package com.example.backend.controllers;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.backend.models.Address;
import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.User;
import com.example.backend.repositories.AddressRepository;
import com.example.backend.repositories.OrderRepository;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.EmailService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class OrderController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Optional<Order> currentOrder = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                if (currentOrder.isPresent()) {
                    Order order = currentOrder.get();
                    model.addAttribute("pedido", order);

                    List<Map<String, Object>> items = buildCartItems(order.getProducts());
                    model.addAttribute("productosCarrito", items);
                    model.addAttribute("hasProductosCarrito", !items.isEmpty());

                    double realTotal = items.stream().mapToDouble(i -> (Double) i.get("lineTotal")).sum();
                    model.addAttribute("precioTotal", String.format("%.2f", realTotal).replace('.', ','));
                } else {
                    model.addAttribute("precioTotal", "0,00");
                    model.addAttribute("productosCarrito", new ArrayList<>());
                    model.addAttribute("hasProductosCarrito", false);
                }
            });
        } else {
            // Anonymous cart using session
            List<Product> sessionCart = (List<Product>) session.getAttribute("cart");
            if (sessionCart == null) {
                sessionCart = new ArrayList<>();
            }
            List<Map<String, Object>> items = buildCartItems(sessionCart);
            model.addAttribute("productosCarrito", items);
            model.addAttribute("hasProductosCarrito", !items.isEmpty());
            double realTotal = items.stream().mapToDouble(i -> (Double) i.get("lineTotal")).sum();
            model.addAttribute("precioTotal", String.format("%.2f", realTotal).replace('.', ','));
        }

        // Also show some recommendations from the database
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.size() > 4) {
            model.addAttribute("recomendados", allProducts.subList(0, 4));
        } else {
            model.addAttribute("recomendados", allProducts);
        }

        return "pages/shopping-cart";
    }

    // Helper to aggregate products into items with quantities and formatted strings
    private List<Map<String, Object>> buildCartItems(List<Product> products) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        Map<Long, Product> prodById = new LinkedHashMap<>();
        for (Product p : products) {
            counts.merge(p.getId(), 1, Integer::sum);
            prodById.putIfAbsent(p.getId(), p);
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : counts.entrySet()) {
            Product p = prodById.get(e.getKey());
            int qty = e.getValue();
            double lineTotal = p.getPrice() * qty;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("image", p.isImage());
            m.put("price", p.getPrice());
            m.put("priceStr", String.format("%.2f", p.getPrice()).replace('.', ','));
            m.put("category", p.getCategory());
            m.put("quantity", qty);
            m.put("lineTotal", lineTotal);
            m.put("lineTotalStr", String.format("%.2f", lineTotal).replace('.', ','));
            m.put("stock", p.getStock());
            items.add(m);
        }
        return items;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, Principal principal,
            jakarta.servlet.http.HttpSession session) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Optional<Order> currentOrderOpt = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                Order order;
                if (currentOrderOpt.isPresent()) {
                    order = currentOrderOpt.get();
                } else {
                    order = new Order();
                    order.setUser(user);
                    order.setStatus("EN PROCESO");
                    order.setOrderDate(java.time.LocalDateTime.now());
                    order.setProducts(new ArrayList<>());
                    order.setTotalPrice(0.0);
                }
                productRepository.findById(productId).ifPresent(product -> {
                    order.getProducts().add(product);
                    order.setTotalPrice(order.getTotalPrice() + product.getPrice());
                    orderRepository.save(order);
                });
            });
        } else {
            // Anonymous session cart
            List<Product> sessionCart = (List<Product>) session.getAttribute("cart");
            if (sessionCart == null) {
                sessionCart = new ArrayList<>();
            }
            Optional<Product> pOpt = productRepository.findById(productId);
            if (pOpt.isPresent()) {
                sessionCart.add(pOpt.get());
                session.setAttribute("cart", sessionCart);
            }
        }
        return "redirect:/shopping-cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, Principal principal,
            jakarta.servlet.http.HttpSession session) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Optional<Order> currentOrderOpt = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                if (currentOrderOpt.isPresent()) {
                    Order order = currentOrderOpt.get();
                    productRepository.findById(productId).ifPresent(product -> {
                        // Remove only one instance of the product
                        List<Product> products = order.getProducts();
                        for (int i = 0; i < products.size(); i++) {
                            if (products.get(i).getId().equals(productId)) {
                                products.remove(i);
                                order.setTotalPrice(Math.max(0, order.getTotalPrice() - product.getPrice()));
                                break;
                            }
                        }
                        orderRepository.save(order);
                    });
                }
            });
        } else {
            // Anonymous session cart
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
    public String updateCartQuantity(@RequestParam Long productId, @RequestParam int quantity, Principal principal,
            jakarta.servlet.http.HttpSession session) {
        int safeQuantity = Math.max(0, quantity);
        final int qty = safeQuantity;
        final Long pid = productId;
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Optional<Order> currentOrderOpt = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                if (currentOrderOpt.isPresent()) {
                    Order order = currentOrderOpt.get();
                    // remove all instances of the product
                    List<Product> products = order.getProducts();
                    products.removeIf(p -> p.getId().equals(pid));
                    // add 'qty' copies
                    productRepository.findById(pid).ifPresent(product -> {
                        for (int i = 0; i < qty; i++) {
                            products.add(product);
                        }
                    });
                    double total = products.stream().mapToDouble(Product::getPrice).sum();
                    order.setTotalPrice(total);
                    orderRepository.save(order);
                }
            });
        } else {
            List<Product> sessionCart = (List<Product>) session.getAttribute("cart");
            if (sessionCart == null) {
                sessionCart = new ArrayList<>();
            }
            // remove all instances
            sessionCart.removeIf(p -> p.getId().equals(pid));
            Optional<Product> pOpt = productRepository.findById(pid);
            if (pOpt.isPresent()) {
                for (int i = 0; i < qty; i++) {
                    sessionCart.add(pOpt.get());
                }
                session.setAttribute("cart", sessionCart);
            }
        }
        return "redirect:/shopping-cart";
    }

    @GetMapping("/payment")
    public String payment(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("direcciones", addressRepository.findByUserId(user.getId()));

                Optional<Order> currentOrder = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                if (currentOrder.isPresent()) {
                    Order order = currentOrder.get();
                    List<Map<String, Object>> items = buildCartItems(order.getProducts());
                    model.addAttribute("productosCarrito", items);
                    double realTotal = items.stream().mapToDouble(i -> (Double) i.get("lineTotal")).sum();
                    model.addAttribute("precioTotal", String.format("%.2f", realTotal).replace('.', ','));
                } else {
                    model.addAttribute("productosCarrito", new ArrayList<>());
                    model.addAttribute("precioTotal", "0,00");
                }
            });
        } else {
            model.addAttribute("productosCarrito", new ArrayList<>());
            model.addAttribute("precioTotal", "0,00");
        }
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            model.addAttribute("_csrf", token);
        }
        return "pages/payment";
    }

    // Add these to your WebController.java

    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam(required = false) Long shipAddressId,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String cardNumber,
            Model model, Principal principal) {

        try {
            // 1. Get the logged-in user
            User user = userRepository.findByUsername(principal.getName()).orElseThrow();

            // 2. Get the user's current order in progress
            Optional<Order> currentOrderOpt = orderRepository.findByUserId(user.getId())
                    .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();

            if (!currentOrderOpt.isPresent()) {
                return "redirect:/shopping-cart?error=noorder";
            }

            Order order = currentOrderOpt.get();

            // 3. Update order with shipping address information
            if (shipAddressId != null) {
                Optional<Address> addressOpt = addressRepository.findById(shipAddressId);
                if (addressOpt.isPresent() && addressOpt.get().getUser() != null
                        && addressOpt.get().getUser().getId().equals(user.getId())) {
                    Address addr = addressOpt.get();
                    order.setShippingAddress(addr.getStreet());
                    order.setCity(addr.getCity());
                    order.setPostalCode(addr.getPostalCode());
                    order.setCountry(addr.getCountry());
                } else {
                    return "redirect:/error/403";
                }
            }
            // 4. Update payment method and order status
            order.setPaymentMethod(
                    cardName != null ? "Card ending in " + cardNumber.substring(Math.max(0, cardNumber.length() - 4))
                            : "Card");
            order.setStatus("PENDIENTE");
            order.setOrderDate(LocalDateTime.now());

            // 5. Save the updated order
            orderRepository.save(order);

            // 6. Send email in background (using @Async)
            emailService.sendInvoiceEmail(order);

            // 7. Redirect to success page with order ID
            return "redirect:/payment-correct?orderId=" + order.getId();
        } catch (Exception e) {
            return "redirect:/shopping-cart?error=payment";
        }
    }

    @GetMapping("/payment-correct")
    public String showSuccessPage(@RequestParam(required = false) Long orderId, Model model) {
        if (orderId != null) {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                model.addAttribute("order", order);
                model.addAttribute("orderId", orderId);
                model.addAttribute("totalPrice", String.format("%.2f", order.getTotalPrice()).replace('.', ','));
                model.addAttribute("productCount", order.getProducts() != null ? order.getProducts().size() : 0);
            }
        }
        return "pages/payment-correct";
    }

    @PostMapping("/address/add")
    public String addAddress(@RequestParam String street,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String country,
            Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Address a = new Address();
                a.setStreet(street);
                a.setCity(city);
                a.setPostalCode(postalCode);
                a.setCountry(country);
                a.setUser(user);
                addressRepository.save(a);
            });
        }
        return "redirect:/payment";
    }

    @GetMapping("/download-invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id, Principal principal) {
        // 1. Buscar el pedido por ID
        Optional<Order> orderOpt = orderRepository.findById(id);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // 2. Seguridad: Comprobar que el pedido pertenece al usuario logueado
            if (!order.getUser().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(403).build();
            }

            try {
                // 3. Generar el PDF
                byte[] pdfBytes = emailService.generatePdfInvoice(order);

                // 4. Configurar la respuesta para que el navegador lo descargue
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
