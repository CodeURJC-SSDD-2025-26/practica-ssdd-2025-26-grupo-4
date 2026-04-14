package com.example.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.engine.jdbc.proxy.BlobProxy;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.Review;
import com.example.backend.models.User;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.OrderRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

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
    private PasswordEncoder passwordEncoder;

    // ===================== PAGE ROUTES (GET) =====================

    @GetMapping("/")
    public String root(Model model) {
        model.addAttribute("productos", productRepository.findAll());
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
    public String shoppingCart(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                Optional<Order> currentOrder = orderRepository.findByUserId(user.getId())
                        .stream().filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst();
                if (currentOrder.isPresent()) {
                    Order order = currentOrder.get();
                    model.addAttribute("pedido", order);
                    model.addAttribute("productosCarrito", order.getProducts());
                    
                    double realTotal = order.getProducts().stream().mapToDouble(Product::getPrice).sum();
                    model.addAttribute("precioBase", String.format("%.2f", realTotal));
                    model.addAttribute("precioDescuento", "0,00");
                    model.addAttribute("precioTotal", String.format("%.2f", realTotal));
                }
            });
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
    public String searchResult(Model model) {
        model.addAttribute("productos", productRepository.findAll());
        return "pages/search-result";
    }

    @GetMapping("/payment")
    public String payment() {
        return "pages/payment";
    }

    @GetMapping("/payment-correct")
    public String paymentCorrect() {
        return "pages/payment_correct";
    }

    @GetMapping("/user-registration")
    public String userRegistration() {
        return "pages/user_registration";
    }

    @GetMapping("/user_registration")
    public String userRegistration2() {
        return "pages/user_registration";
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
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                user.setUsername(username);
                user.setEmail(email);

                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        user.setProfilePicture(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                        user.setHasPicture(true);
                    } catch (IOException e) {
                        // Ignore image upload errors
                    }
                }

                userRepository.save(user);
            });
        }
        return "redirect:/profile";
    }
}
