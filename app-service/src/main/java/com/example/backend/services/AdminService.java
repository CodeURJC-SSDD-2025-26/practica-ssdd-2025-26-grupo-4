package com.example.backend.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.Review;
import com.example.backend.models.User;
import com.example.backend.repositories.OrderRepository;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;

@Service
public class AdminService {

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

    // ================= DASHBOARD =================

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        List<Order> salesOrders = orderRepository.findByStatusIn(
                Arrays.asList("ENTREGADO", "ENVIADO"));

        data.put("totalProductos", products.size());
        data.put("totalUsuarios", users.size());
        data.put("totalPedidos", orders.size());

        double totalIngresos = salesOrders.stream()
                .mapToDouble(Order::getTotalPrice).sum();

        data.put("totalIngresos", String.format("%.2f", totalIngresos).replace('.', ','));
        data.put("pedidosRecientes", orders);

        // categorías
        Map<String, Long> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        data.put("chartCategoryLabels", formatLabels(productsByCategory.keySet()));
        data.put("chartCategoryData", formatValues(productsByCategory.values()));

        // ventas por categoría
        Map<String, Double> salesByCategory = new LinkedHashMap<>();
        for (Order order : salesOrders) {
            if (order.getProducts() != null) {
                for (Product p : order.getProducts()) {
                    salesByCategory.merge(p.getCategory(), p.getPrice(), Double::sum);
                }
            }
        }

        data.put("chartSalesLabels", formatLabels(salesByCategory.keySet()));
        data.put("chartSalesData", formatDoubleValues(salesByCategory.values()));

        // stock
        data.put("stockInStock", products.stream().filter(p -> p.getStock() > 10).count());
        data.put("stockLowStock", products.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 10).count());
        data.put("stockOutOfStock", products.stream().filter(p -> p.getStock() == 0).count());

        return data;
    }

    // ================= PRODUCTS =================

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Optional<Product> getProduct(Long id) {
        return productRepository.findById(id);
    }

    public void createProduct(String nombre, String descripcion, String categoria,
                             double precio, int stock, MultipartFile imageFile) throws IOException {

        Product product = new Product();
        product.setName(nombre);
        product.setDescription(descripcion);
        product.setCategory(categoria);
        product.setPrice(precio);
        product.setStock(stock);

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageFile(BlobProxy.generateProxy(
                    imageFile.getInputStream(), imageFile.getSize()));
            product.setImage(true);
        }

        productRepository.save(product);
    }

    public void updateProduct(Long id, String nombre, String descripcion, String categoria,
                             double precio, int stock, MultipartFile imageFile) throws IOException {

        productRepository.findById(id).ifPresent(product -> {
            product.setName(nombre);
            product.setDescription(descripcion);
            product.setCategory(categoria);
            product.setPrice(precio);
            product.setStock(stock);

            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    product.setImageFile(BlobProxy.generateProxy(
                            imageFile.getInputStream(), imageFile.getSize()));
                    product.setImage(true);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            productRepository.save(product);
        });
    }

    public void softDeleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    // ================= USERS =================

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    public void createUser(String username, String email, String password,
                           String rol, MultipartFile imageFile) throws IOException {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));

        user.setRoles(buildRoles(rol));

        if (imageFile != null && !imageFile.isEmpty()) {
            user.setProfilePicture(BlobProxy.generateProxy(
                    imageFile.getInputStream(), imageFile.getSize()));
            user.setHasPicture(true);
        }

        userRepository.save(user);
    }

    public void updateUser(Long id, String nombre, String email,
                           String password, String rol, MultipartFile imageFile) throws IOException {

        userRepository.findById(id).ifPresent(user -> {
            user.setUsername(nombre);
            user.setEmail(email);

            if (password != null && !password.isBlank()) {
                user.setEncodedPassword(passwordEncoder.encode(password));
            }

            user.setRoles(buildRoles(rol));

            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    user.setProfilePicture(BlobProxy.generateProxy(
                            imageFile.getInputStream(), imageFile.getSize()));
                    user.setHasPicture(true);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            userRepository.save(user);
        });
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ================= ORDERS =================

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    public void updateOrderStatus(Long id, String status) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status.toUpperCase());
            orderRepository.save(order);
        });
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // ================= REVIEWS =================

    public List<Review> getReviews() {
        return reviewRepository.findAll();
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public void replyReview(Long id, String reply) {
        reviewRepository.findById(id).ifPresent(r -> {
            r.setAdminReply(reply);
            reviewRepository.save(r);
        });
    }

    public void deleteReviewReply(Long id) {
        reviewRepository.findById(id).ifPresent(r -> {
            r.setAdminReply(null);
            reviewRepository.save(r);
        });
    }

    // ================= HELPERS =================

    private List<String> buildRoles(String rol) {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        if ("Administrador".equals(rol)) {
            roles.add("ROLE_ADMIN");
        }
        return roles;
    }

    private String formatLabels(Collection<String> values) {
        return values.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
    }

    private String formatValues(Collection<?> values) {
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String formatDoubleValues(Collection<Double> values) {
        return values.stream()
                .map(v -> String.format("%.2f", v))
                .collect(Collectors.joining(","));
    }
}