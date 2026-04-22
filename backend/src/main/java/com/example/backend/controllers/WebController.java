package com.example.backend.controllers;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.backend.models.Address;
import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.Review;
import com.example.backend.models.User;
import com.example.backend.repositories.AddressRepository;
import com.example.backend.repositories.OrderRepository;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.EmailService;
import com.example.backend.services.RecommendationService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private EmailService emailService;

    // ===================== PAGE ROUTES (GET) =====================

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        // Get the principal (logged user)
        Principal principal = request.getUserPrincipal();
        boolean isLoggedIn = principal != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 1. Hardware News Section
        // Your HTML expects 'productos' for this section
        List<Product> hardwareNews = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .limit(8)
                .collect(Collectors.toList());
        model.addAttribute("productos", hardwareNews);

        // 2. Recommendations Section ("Te podría interesar")
        if (isLoggedIn) {
            Optional<User> userOpt = userRepository.findByUsername(principal.getName());
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
        model.addAttribute("productos", productRepository.findAll());
        return "index";
    }

    @GetMapping("/item-detail")
    public String itemDetail(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            productRepository.findById(id).ifPresent(product -> {
                model.addAttribute("producto", product);
                model.addAttribute("reviews", reviewRepository.findByProductId(id));
            });
        }
        return "pages/item-detail";
    }

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

    @GetMapping("/search-result")
    public String searchResult(@RequestParam(value = "category", required = false) String category, Model model) {
        if (category != null && !category.isBlank()) {
            model.addAttribute("productos", productRepository.findByCategory(category));
            model.addAttribute("categoryName", category);
        } else {
            model.addAttribute("productos", productRepository.findAll());
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
            // Lógica de limpieza de nulos
            String searchName = (name != null && !name.trim().isEmpty()) ? name.trim().toLowerCase() : null;
            String searchCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
            String searchBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : null;

            // Procesamiento inteligente de la búsqueda (ej: "procesadores intel")
            if (searchName != null) {
                if (searchName.contains("procesador") || searchName.contains("cpu")) {
                    searchCategory = "CPU";
                    searchName = searchName.replaceAll("procesadores|procesador|cpu", "").trim();
                } else if (searchName.contains("grafica") || searchName.contains("gráfica")
                        || searchName.contains("gpu") || searchName.contains("tarjeta")) {
                    searchCategory = "GPU";
                    searchName = searchName.replaceAll("tarjetas?|gráficas?|graficas?|gpu|de|video", "").trim();
                } else if (searchName.contains("placa") || searchName.contains("base")
                        || searchName.contains("motherboard")) {
                    searchCategory = "Motherboard";
                    searchName = searchName.replaceAll("placas?|bases?|motherboards?", "").trim();
                } else if (searchName.contains("ram") || searchName.contains("memoria")) {
                    searchCategory = "RAM";
                    searchName = searchName.replaceAll("memorias?|ram", "").trim();
                } else if (searchName.contains("disco") || searchName.contains("duro") || searchName.contains("ssd")
                        || searchName.contains("almacenamiento")) {
                    searchCategory = "SSD";
                    searchName = searchName.replaceAll("discos?|duros?|almacenamiento|ssd", "").trim();
                } else if (searchName.contains("fuente") || searchName.contains("alimentacion")
                        || searchName.contains("alimentación") || searchName.contains("powersupply")) {
                    searchCategory = "PowerSupply";
                    searchName = searchName.replaceAll("fuentes?|de|alimentación|alimentacion|powersupply", "").trim();
                } else if (searchName.contains("refrigeracion") || searchName.contains("refrigeración")
                        || searchName.contains("cooling") || searchName.contains("ventilador")
                        || searchName.contains("disipador")) {
                    searchCategory = "Cooling";
                    searchName = searchName
                            .replaceAll("refrigeración|refrigeracion|cooling|ventiladores?|disipadores?", "").trim();
                }

                if (searchName.isEmpty()) {
                    searchName = null;
                }
            }

            // Lógica de ordenación
            Sort sortOrder = Sort.unsorted();
            if ("priceAsc".equals(sort)) {
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
            } else if ("priceDesc".equals(sort)) {
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
            }

            List<Product> results = productRepository.findWithFilters(searchName, searchCategory, searchBrand, minPrice,
                    maxPrice, sortOrder);

            model.addAttribute("productos", results);
            model.addAttribute("query", name != null ? name : "");
            model.addAttribute("category", category != null ? category : "");
            model.addAttribute("brand", brand != null ? brand : "");
            model.addAttribute("currentSort", sort != null ? sort : "");

            if (name != null && !name.trim().isEmpty()) {
                model.addAttribute("searchTerm", name);
            } else {
                model.addAttribute("searchTerm", null);
            }

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

<<<<<<< HEAD
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
                if (addressOpt.isPresent()) {
                    Address addr = addressOpt.get();
                    order.setShippingAddress(addr.getStreet());
                    order.setCity(addr.getCity());
                    order.setPostalCode(addr.getPostalCode());
                    order.setCountry(addr.getCountry());
                }
            }

            // 4. Update payment method and order status
            order.setPaymentMethod(cardName != null ? "Card ending in " + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "Card");
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
=======
    @PostMapping("/payment")
    public String processPayment(@RequestParam(required = false) Long shipAddress,
                                 @RequestParam(required = false) String paymentMethod,
                                 Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) return "redirect:/login";
        User user = userOpt.get();
        Optional<Order> orderOpt = orderRepository.findByUserId(user.getId()).stream()
                .filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
        if (orderOpt.isEmpty()) return "redirect:/shopping-cart";
        Order order = orderOpt.get();
        if (order.getProducts() == null || order.getProducts().isEmpty()) return "redirect:/shopping-cart";

        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "card");
        if (shipAddress != null) {
            Optional<Address> addrOpt = addressRepository.findById(shipAddress);
            if (addrOpt.isPresent() && addrOpt.get().getUser() != null && addrOpt.get().getUser().getId().equals(user.getId())) {
                Address a = addrOpt.get();
                order.setShippingAddress(a.getStreet());
                order.setCity(a.getCity());
                order.setPostalCode(a.getPostalCode());
                order.setCountry(a.getCountry());
            }
        }
        order.setStatus("PENDIENTE");
        order.setOrderDate(java.time.LocalDateTime.now());
        double total = order.getProducts().stream().mapToDouble(Product::getPrice).sum();
        order.setTotalPrice(total);
        orderRepository.save(order);

        return "redirect:/payment-correct";
>>>>>>> 17168bf01887607474dc56d3b2c9f6ab225efe12
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

    @GetMapping("/user-registration")
    public String userRegistration() {
        return "pages/user_registration";
    }

    @GetMapping("/user_registration")
    public String userRegistration2() {
        return "pages/user_registration";
    }
     @PostMapping("/user-registration")
    public String registerUser(
        @RequestParam String username,
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam String repeatPassword,
        Model model) {
    if (!password.equals(repeatPassword)) {
        model.addAttribute("error", "Las contraseñas no coinciden");
        return "user_registration"; 
    }
    if (userRepository.findByUsername(username).isPresent()) {
        model.addAttribute("error", "El usuario ya existe");
        return "user_registration";
    }
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setEncodedPassword(passwordEncoder.encode(password)); 
    user.setRoles(Arrays.asList("ROLE_USER"));

    userRepository.save(user);

    return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "pages/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("usuario", user);
                model.addAttribute("reviews", reviewRepository.findByUserId(user.getId()));
                model.addAttribute("pedidos", orderRepository.findByUserId(user.getId()));
            });
        }
        return "pages/profile";
    }

    @GetMapping("/admin/admin-dashboard")
    public String adminDashboard(Model model) {
        List<Product> allProducts = productRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();

        // Sales = orders with status ENTREGADO or ENVIADO
        List<Order> salesOrders = orderRepository.findByStatusIn(Arrays.asList("ENTREGADO", "ENVIADO"));

        // Summary counters
        model.addAttribute("totalProductos", allProducts.size());
        model.addAttribute("totalUsuarios", allUsers.size());
        model.addAttribute("totalPedidos", allOrders.size());

        // Revenue only from completed sales (ENTREGADO + ENVIADO)
        double totalIngresos = salesOrders.stream().mapToDouble(Order::getTotalPrice).sum();
        model.addAttribute("totalIngresos", String.format("%.2f", totalIngresos).replace('.', ','));

        // Recent orders (all)
        model.addAttribute("pedidosRecientes", allOrders);

        // Chart data: products per category
        Map<String, Long> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
        model.addAttribute("chartCategoryLabels", String.join(",",
                productsByCategory.keySet().stream().map(k -> "'" + k + "'").collect(Collectors.toList())));
        model.addAttribute("chartCategoryData", String.join(",",
                productsByCategory.values().stream().map(String::valueOf).collect(Collectors.toList())));

        // Chart data: sales revenue per category (from ENTREGADO + ENVIADO orders)
        Map<String, Double> salesByCategory = new LinkedHashMap<>();
        for (Order order : salesOrders) {
            if (order.getProducts() != null) {
                for (Product p : order.getProducts()) {
                    salesByCategory.merge(p.getCategory(), p.getPrice(), Double::sum);
                }
            }
        }
        model.addAttribute("chartSalesLabels", String.join(",",
                salesByCategory.keySet().stream().map(k -> "'" + k + "'").collect(Collectors.toList())));
        model.addAttribute("chartSalesData", String.join(",",
                salesByCategory.values().stream().map(v -> String.format("%.2f", v)).collect(Collectors.toList())));

        // Chart data: inventory status
        long inStock = allProducts.stream().filter(p -> p.getStock() > 10).count();
        long lowStock = allProducts.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 10).count();
        long outOfStock = allProducts.stream().filter(p -> p.getStock() == 0).count();
        model.addAttribute("stockInStock", inStock);
        model.addAttribute("stockLowStock", lowStock);
        model.addAttribute("stockOutOfStock", outOfStock);

        return "pages/admin/admin-dashboard";
    }

    @GetMapping("/admin/item-create")
    public String itemCreate() {
        return "pages/admin/item-create";
    }

    @GetMapping("/admin/item-edit")
    public String itemEdit(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            productRepository.findById(id).ifPresent(product -> {
                model.addAttribute("producto", product);
            });
        }
        return "pages/admin/item-edit";
    }

    @GetMapping("/admin/item-list")
    public String itemList(Model model) {
        model.addAttribute("productos", productRepository.findAll());
        return "pages/admin/item-list";
    }

    @GetMapping("/admin/order-list")
    public String orderList(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("pedidos", orders);

        long totalOrders = orders.size();
        double totalIncome = orders.stream()
                .filter(o -> "ENTREGADO".equals(o.getStatus()) || "ENVIADO".equals(o.getStatus()))
                .mapToDouble(Order::getTotalPrice)
                .sum();
        long pendingOrders = orders.stream()
                .filter(o -> "PENDIENTE".equals(o.getStatus()))
                .count();
        long cancelledOrders = orders.stream()
                .filter(o -> "CANCELADO".equals(o.getStatus()))
                .count();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalIncome", String.format("%.2f", totalIncome));
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "pages/admin/order-list";
    }

    @GetMapping("/admin/order-edit")
    public String orderEdit(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            orderRepository.findById(id).ifPresent(order -> {
                model.addAttribute("pedido", order);
            });
        }
        return "pages/admin/order-edit";
    }

    @GetMapping("/admin/review-list")
    public String reviewList(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "pages/admin/review-list";
    }

    @GetMapping("/admin/user-create")
    public String userCreate() {
        return "pages/admin/user-create";
    }

    @GetMapping("/admin/user-edit")
    public String userEdit(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            userRepository.findById(id).ifPresent(user -> {
                model.addAttribute("usuario", user);
            });
        }
        return "pages/admin/user-edit";
    }

    @GetMapping("/admin/user-list")
    public String userList(Model model) {
        model.addAttribute("usuarios", userRepository.findAll());
        return "pages/admin/user-list";
    }

    // ===================== CRUD ENDPOINTS (POST) =====================

    // --- PRODUCTS ---

    @PostMapping("/admin/item-create")
    public String createProduct(@RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam String categoria,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        Product product = new Product();
        product.setName(nombre);
        product.setDescription(descripcion);
        product.setCategory(categoria);
        product.setPrice(precio);
        product.setStock(stock);

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageFile(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
            product.setImage(true);
        }

        productRepository.save(product);
        return "redirect:/admin/item-list";
    }

    @PostMapping("/admin/item-edit")
    public String editProduct(@RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam String categoria,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isPresent()) {
            Product product = optProduct.get();
            product.setName(nombre);
            product.setDescription(descripcion);
            product.setCategory(categoria);
            product.setPrice(precio);
            product.setStock(stock);

            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageFile(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                product.setImage(true);
            }

            productRepository.save(product);
        }
        return "redirect:/admin/item-list";
    }

    @PostMapping("/admin/item-delete")
    public String deleteProduct(@RequestParam Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/item-list";
    }

    // --- USERS ---

    @PostMapping("/admin/user-create")
    public String createUser(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        if ("Administrador".equals(rol)) {
            roles.add("ROLE_ADMIN");
        }
        user.setRoles(roles);

        if (imageFile != null && !imageFile.isEmpty()) {
            user.setProfilePicture(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
            user.setHasPicture(true);
        }

        userRepository.save(user);
        return "redirect:/admin/user-list";
    }

    @PostMapping("/admin/user-edit")
    public String editUser(@RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String contrasena,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setUsername(nombre);
            user.setEmail(email);

            if (contrasena != null && !contrasena.isBlank()) {
                user.setEncodedPassword(passwordEncoder.encode(contrasena));
            }

            List<String> roles = new ArrayList<>();
            roles.add("ROLE_USER");
            if ("Administrador".equals(rol)) {
                roles.add("ROLE_ADMIN");
            }
            user.setRoles(roles);

            if (imageFile != null && !imageFile.isEmpty()) {
                user.setProfilePicture(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                user.setHasPicture(true);
            }

            userRepository.save(user);
        }
        return "redirect:/admin/user-list";
    }

    @PostMapping("/admin/user-delete")
    public String deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/user-list";
    }

    // --- ORDERS ---

    @PostMapping("/admin/order-edit")
    public String updateOrderStatus(@RequestParam Long id,
            @RequestParam String status,
            @RequestParam(required = false) String emailMsg,
            @RequestParam(required = false) Boolean notifyClient) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status.toUpperCase());
            // In a real app, we would send an email here if notifyClient is true
            orderRepository.save(order);
        });
        return "redirect:/admin/order-edit?id=" + id;
    }

    @PostMapping("/admin/order-delete")
    public String deleteOrder(@RequestParam Long id) {
        orderRepository.deleteById(id);
        return "redirect:/admin/order-list";
    }

    // --- REVIEWS ---

    @PostMapping("/admin/review-delete")
    public String deleteReview(@RequestParam Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/review-list";
    }

    @PostMapping("/admin/review-reply")
    public String replyReview(@RequestParam Long id, @RequestParam String reply) {
        Optional<Review> optReview = reviewRepository.findById(id);
        if (optReview.isPresent()) {
            Review review = optReview.get();
            review.setAdminReply(reply);
            reviewRepository.save(review);
        }
        return "redirect:/admin/review-list";
    }

    @PostMapping("/admin/review-reply-delete")
    public String deleteReviewReply(@RequestParam Long id) {
        Optional<Review> optReview = reviewRepository.findById(id);
        if (optReview.isPresent()) {
            Review review = optReview.get();
            review.setAdminReply(null);
            reviewRepository.save(review);
        }
        return "redirect:/admin/review-list";
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

    // --- PROFILE UPDATE ---

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) MultipartFile imageFile,
            Principal principal) throws IOException {
        if (principal != null) {
            Optional<User> optUser = userRepository.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                User user = optUser.get();
                user.setUsername(username);
                user.setEmail(email);

                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        user.setProfilePicture(
                                BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                        user.setHasPicture(true);
                    } catch (IOException e) {
                        // Ignore image upload errors
                    }
                }

                userRepository.save(user);

                // Re-authenticate with the new username so the SecurityContext is up-to-date
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        username, currentAuth.getCredentials(), currentAuth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        }
        return "redirect:/profile";
    }

<<<<<<< HEAD
=======
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

    // Allow users (owner) or admins to delete/edit their own addresses
    @PostMapping("/address/delete")
    public String deleteAddress(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        Optional<Address> optAddr = addressRepository.findById(id);
        if (optAddr.isEmpty()) return "redirect:/payment";
        Address addr = optAddr.get();

        Optional<User> currentUserOpt = userRepository.findByUsername(principal.getName());
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin || (addr.getUser() != null && currentUserOpt.isPresent()
                && addr.getUser().getId().equals(currentUserOpt.get().getId()))) {
            addressRepository.deleteById(id);
            return "redirect:/payment";
        }
        return "redirect:/error/403";
    }

>>>>>>> 46938b821995fcb591ffbba340d0044dc9c4fbef
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }

<<<<<<< HEAD
    @GetMapping("/error/403")
    public String accessDenied() {
        return "error-403";
=======
    // Convenience attribute available to all templates: current logged user id (or null)
    @ModelAttribute("currentUserId")
    public Long currentUserId(Principal principal) {
        if (principal == null) return null;
        Optional<User> u = userRepository.findByUsername(principal.getName());
        return u.map(User::getId).orElse(null);
    }

    @GetMapping("/error/403")
    public String accessDenied() {
          return "error-403";
>>>>>>> 46938b821995fcb591ffbba340d0044dc9c4fbef
    }
}
