package com.example.backend.services;

import com.example.backend.models.*;
import com.example.backend.repositories.*;
import jakarta.annotation.PostConstruct;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class DatabaseInitializer {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostConstruct
    public void init() {

        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Users (Admin and Customer)
        User admin = new User();
        admin.setUsername("admin");
        admin.setEncodedPassword("password"); // Will be encoded later with Security
        admin.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(admin);

        User customer = new User();
        customer.setUsername("user");
        customer.setEncodedPassword("password");
        customer.setRoles(Arrays.asList("USER"));
        userRepository.save(customer);

        // 2. Create Products (Sample Components)

        // CPUs

        Product p1 = new Product();
        p1.setName("AMD Ryzen 7 9800X3D 4.7/5.2GHz");
        p1.setDescription(
                "Procesador AMD Ryzen 7 9800X3D con tecnología 3D V-Cache de segunda generación. 8 núcleos y 16 hilos. Ideal para gaming de alto rendimiento.");
        p1.setPrice(464.95);
        p1.setStock(24);
        p1.setCategory("Procesadores");
        setProductImage(p1, "../resources/static/assets/images/amd-ryzen-7-9800x3d/amd-ryzen-7-9800x3d-side.jpg");
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Intel Core i5-13600K");
        p2.setDescription("Excelente rendimiento para gaming y productividad media.");
        p2.setPrice(320.00);
        p2.setStock(10);
        p2.setCategory("Procesadores");
        productRepository.save(p2);

        // GPUs

        Product p3 = new Product();
        p3.setName("NVIDIA RTX 4080 Super");
        p3.setDescription("Experience ultra-performance gaming with ray tracing.");
        p3.setPrice(1099.00);
        p3.setStock(5);
        p3.setCategory("GPU");
        productRepository.save(p3);

        Product p4 = new Product();
        p4.setName("AMD Radeon RX 7900 XTX");
        p4.setDescription("La bestia de AMD para 4K nativo.");
        p4.setPrice(950.00);
        p4.setStock(0); // PRODUCTO AGOTADO para pruebas
        p4.setCategory("Tarjetas Gráficas");
        productRepository.save(p4);

        Product p5 = new Product();
        p5.setName("NVIDIA RTX 4060 Ti");
        p5.setDescription("Perfecta para 1080p con DLSS 3.");
        p5.setPrice(399.00);
        p5.setStock(30);
        p5.setCategory("Tarjetas Gráficas");
        productRepository.save(p5);

        // Motherboards

        Product p6 = new Product();
        p6.setName("ASUS ROG Strix Z790-E");
        p6.setDescription("Placa base de gama alta para Intel de 13ª y 14ª gen.");
        p6.setPrice(450.00);
        p6.setStock(8);
        p6.setCategory("Placas Base");
        productRepository.save(p6);

        Product p7 = new Product();
        p7.setName("MSI B650 Tomahawk WiFi");
        p7.setDescription("La mejor opción calidad-precio para AM5.");
        p7.setPrice(210.00);
        p7.setStock(12);
        p7.setCategory("Placas Base");
        productRepository.save(p7);

        // 3. Reviews for products
        Review r1 = new Review();
        r1.setScore(5);
        r1.setComment("El Ryzen 7 9800X3D es una bestia para gaming.");
        r1.setDate(LocalDateTime.now().minusDays(2));
        r1.setProduct(p1); // El procesador
        r1.setUser(customer);
        reviewRepository.save(r1);

        Review r2 = new Review();
        r2.setScore(4);
        r2.setComment("Buena placa, aunque la BIOS es algo compleja.");
        r2.setDate(LocalDateTime.now().minusDays(1));
        r2.setProduct(p6); // La placa ASUS
        r2.setUser(customer);
        reviewRepository.save(r2);

        // 4. Orders
        // Order completed
        Order o1 = new Order();
        o1.setUser(customer);
        o1.setOrderDate(LocalDateTime.now().minusWeeks(1));
        o1.setTotalPrice(674.95); // (p1 + p7 aprox)
        o1.setStatus("ENTREGADO");
        // Importante: Si tu entidad Order tiene una lista de productos, añádelos
        o1.setProducts(Arrays.asList(p1, p7));
        orderRepository.save(o1);

        // Order "In Process"
        Order o2 = new Order();
        o2.setUser(customer);
        o2.setOrderDate(LocalDateTime.now());
        o2.setTotalPrice(1099.00); // El precio de la 4080
        o2.setStatus("EN PROCESO");
        o2.setProducts(Arrays.asList(p2));
        orderRepository.save(o2);
    }

    private void setProductImage(Product product, String imagePath) {
        try {
            // Cargamos la imagen desde la carpeta de recursos
            Resource image = new ClassPathResource(imagePath);
            if (image.exists()) {
                byte[] data = image.getContentAsByteArray();
                product.setImageFile(BlobProxy.generateProxy(data));
                product.setImage(true);
            }
        } catch (IOException e) {
            // Si hay un error al cargar la imagen, simplemente marcamos que no tiene
            product.setImage(false);
        }
    }
}