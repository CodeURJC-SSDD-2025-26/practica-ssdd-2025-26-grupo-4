package com.example.backend.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.models.Address;
import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.example.backend.models.User;
import com.example.backend.repositories.AddressRepository;
import com.example.backend.repositories.OrderRepository;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private EmailService emailService;

    public Optional<Order> getActiveOrderForUser(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> orderRepository.findByUserId(user.getId()).stream()
                        .filter(o -> "EN PROCESO".equals(o.getStatus())).findFirst());
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<Order> getSalesOrders() {
        return orderRepository.findByStatusIn(java.util.Arrays.asList("ENTREGADO", "ENVIADO"));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Address> getUserAddresses(String username) {
        return userRepository.findByUsername(username)
                .map(user -> addressRepository.findByUserId(user.getId()))
                .orElse(new ArrayList<>());
    }

    public Page<Order> getOrdersByUserUsername(String username, Pageable pageable) {
        return orderRepository.findByUserUsername(username, pageable);
    }

    // Add product to user's cart
    public void addProductToUserCart(String username, Long productId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            Order order = getActiveOrderForUser(username).orElseGet(() -> {
                Order newOrder = new Order();
                newOrder.setUser(user);
                newOrder.setStatus("EN PROCESO");
                newOrder.setOrderDate(LocalDateTime.now());
                newOrder.setProducts(new ArrayList<>());
                newOrder.setTotalPrice(0.0);
                return newOrder;
            });

            productRepository.findById(productId).ifPresent(product -> {
                order.getProducts().add(product);
                order.setTotalPrice(order.getTotalPrice() + product.getPrice());
                orderRepository.save(order);
            });
        });
    }

    // Remove product from user's cart
    public void removeProductFromUserCart(String username, Long productId) {
        getActiveOrderForUser(username).ifPresent(order -> {
            productRepository.findById(productId).ifPresent(product -> {
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
        });
    }

    // Update product quantity in user's cart
    public void updateProductQuantityInUserCart(String username, Long productId, int quantity) {
        int safeQuantity = Math.max(0, quantity);
        getActiveOrderForUser(username).ifPresent(order -> {
            List<Product> products = order.getProducts();
            products.removeIf(p -> p.getId().equals(productId)); // Remove all instances

            productRepository.findById(productId).ifPresent(product -> {
                for (int i = 0; i < safeQuantity; i++) {
                    products.add(product);
                }
            });

            double total = products.stream().mapToDouble(Product::getPrice).sum();
            order.setTotalPrice(total);
            orderRepository.save(order);
        });
    }

    // Checkout logic
    public Order processPayment(String username, Long shipAddressId, String cardName, String cardNumber)
            throws Exception {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("Usuario no encontrado"));
        Order order = getActiveOrderForUser(username).orElseThrow(() -> new Exception("No hay un pedido activo"));

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
                throw new Exception("Dirección no válida");
            }
        }

        order.setPaymentMethod(
                cardName != null ? "Card ending in " + cardNumber.substring(Math.max(0, cardNumber.length() - 4))
                        : "Card");
        order.setStatus("PENDIENTE");
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);
        emailService.sendInvoiceEmail(order); // Delegate to utility-service

        return order;
    }

    public void addUserAddress(String username, String street, String city, String postalCode, String country) {
        userRepository.findByUsername(username).ifPresent(user -> {
            Address a = new Address();
            a.setStreet(street);
            a.setCity(city);
            a.setPostalCode(postalCode);
            a.setCountry(country);
            a.setUser(user);
            addressRepository.save(a);
        });
    }

    // Presentation logic moved here to keep the controller clean
    public List<Map<String, Object>> buildCartItems(List<Product> products) {
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

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) throws Exception {
        orderRepository.deleteById(id);
    }

}