package com.example.backend.services;

import com.example.backend.models.*;
import com.example.backend.repositories.*;
import jakarta.annotation.PostConstruct;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Users (Admin and Customer)
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setEncodedPassword(passwordEncoder.encode("password")); // Will be encoded later with Security
        admin.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        userRepository.save(admin);

        User customer = new User();
        customer.setUsername("user");
        customer.setEmail("user@gmail.com");
        customer.setEncodedPassword(passwordEncoder.encode("password"));
        customer.setRoles(Arrays.asList("ROLE_USER"));
        userRepository.save(customer);

        // 2. Create Products (Sample Components)

        // CPUs

        Product p1 = new Product();
        p1.setName("AMD Ryzen 7 9800X3D 4.7/5.2GHz");
        p1.setDescription(
                "Tecnología 3D V-Cache de segunda generación. 8 núcleos y 16 hilos. Ideal para gaming de alto rendimiento.");
        p1.setPrice(464.95);
        p1.setStock(24);
        p1.setCategory("CPU");
        setProductImage(p1, "../resources/static/assets/images/amd-ryzen-7-9800x3d/amd-ryzen-7-9800x3d-side.jpg");
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("AMD Ryzen 7 7800X3D 4.2 GHz/5 GHz");
        p2.setDescription(
                "Saca el máximo provecho de tu setup gaming y creativo gracias al AMD Ryzen 7 7800X3D, el procesador que redefine el rendimiento en juegos y cargas profesionales exigentes.");
        p2.setPrice(349.90);
        p2.setStock(10);
        p2.setCategory("CPU");
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setName("Intel Core i7-14700K 3.4/5.6GHz Box");
        p3.setDescription(
                "Ofrece un total de 8 núcleos P-Core y 12 núcleos E-Core, con una frecuencia base de 3,40 GHz para los P-Core y una impresionante frecuencia máxima de 5,60 GHz.");
        p3.setPrice(390.90);
        p3.setStock(15);
        p3.setCategory("CPU");
        productRepository.save(p3);

        // GPUs

        Product p4 = new Product();
        p4.setName("Tarjeta Gráfica ASUS TUF Gaming GeForce RTX 4070 Ti OC Edition 12GB GDDR6X DLSS3");
        p4.setDescription(
                "Cuentan con la tecnología de la arquitectura ultra eficiente NVIDIA Ada Lovelace, que ofrece un salto espectacular tanto en rendimiento como en gráficos con tecnología de IA.");
        p4.setPrice(749.00);
        p4.setStock(4);
        p4.setCategory("GPU");
        productRepository.save(p4);

        Product p5 = new Product();
        p5.setName("ASUS PRIME AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4");
        p5.setDescription(
                "Rendimiento de alto nivel para gaming 4K, edición profesional y tareas de IA, gracias a sus 16GB GDDR6 y arquitectura AMD RDNA 3.");
        p5.setPrice(699.00);
        p5.setStock(30);
        p5.setCategory("GPU");
        productRepository.save(p5);

        Product p6 = new Product();
        p6.setName("MSI GeForce RTX 5080 VENTUS 3X OC 16GB GDDR7 Reflex 2 RTX AI DLSS4");
        p6.setDescription(
                "Su eficiente solución térmica está envuelta en una carcasa resistente con una estética neutra, lo que permite que esta elegante tarjeta gráfica se integre perfectamente en cualquier sistema.");
        p6.setPrice(1329.90);
        p6.setStock(8);
        p6.setCategory("GPU");
        productRepository.save(p6);

        // Motherboards

        Product p7 = new Product();
        p7.setName("Gigabyte B760 DS3H DDR4");
        p7.setDescription(
                "Equipada con una solución de energía mejorada, los últimos estándares de almacenamiento y una conectividad excepcional para permitir un rendimiento optimizado para juegos.");
        p7.setPrice(109.99);
        p7.setStock(12);
        p7.setCategory("Motherboard");
        productRepository.save(p7);

        Product p8 = new Product();
        p8.setName("MSI B850 GAMING PLUS WIFI");
        p8.setDescription(
                "Soporta procesadores AMD Ryzen 9000/8000/7000 en zócalo AM5 y hasta 256 GB DDR5, permitiéndote equipar tu equipo con lo último en rendimiento computacional y memoria de alta frecuencia compatible con overclocking para gaming avanzado y cargas profesionales intensivas.");
        p8.setPrice(189.90);
        p8.setStock(2);
        p8.setCategory("Motherboard");
        productRepository.save(p8);

        // Cooling

        Product p9 = new Product();
        p9.setName("Corsair NAUTILUS 240 RS ARGB Kit Refrigeración Líquida 240mm Negro");
        p9.setDescription(
                "Ofrece un enfriamiento eficiente y silencioso con una conectividad sencilla: no requiere controlador, se conecta directamente a la placa base.");
        p9.setPrice(89.90);
        p9.setStock(24);
        p9.setCategory("Cooling");
        productRepository.save(p9);

        Product p10 = new Product();
        p10.setName("Nfortec VELA X 5Pipes Ventilador CPU 120mm");
        p10.setDescription(
                "Vela X incorpora un disipador mejorado y un ventilador para incrementar su capacidad adaptándose a las demandas de los nuevos procesadores.");
        p10.setPrice(34.99);
        p10.setStock(37);
        p10.setCategory("Cooling");
        productRepository.save(p10);

        // RAM

        Product p11 = new Product();
        p11.setName("Kingston FURY Beast DDR4 3200 MHz 16GB 2x8GB CL16");
        p11.setDescription(
                "Es la elección perfecta para los desarrolladores de sistemas y para quienes buscan una actualización para regenerar un sistema lento. La memoria DDR4 FURY Beast de Kingston, que ofrece una actualización Plug N Play sencilla y fácil de usar.");
        p11.setPrice(151.99);
        p11.setStock(11);
        p11.setCategory("RAM");
        productRepository.save(p11);

        Product p12 = new Product();
        p12.setName("Corsair Vengeance RGB 32GB 2x16GB DDR5 6000MHz CL38 Intel XMP AMD EXPO Gris");
        p12.setDescription(
                "Impulsa al máximo tu experiencia gaming y profesional con la memoria RAM Corsair Vengeance RGB 32GB DDR5 6000MHz CL38, perfecta para usuarios exigentes que buscan velocidad, estabilidad y personalización total.");
        p12.setPrice(419.99);
        p12.setStock(21);
        p12.setCategory("RAM");
        productRepository.save(p12);

        // PowerSupply

        Product p13 = new Product();
        p13.setName("MSI MAG A650BN 650W 80 Plus Bronze");
        p13.setDescription(
                "Sus características principales incluyen la certificación 80 PLUS Bronze, diseño de circuito de CC a CC, carril único de 12 V, PFC activo y ventilador de bajo ruido.");
        p13.setPrice(57.99);
        p13.setStock(34);
        p13.setCategory("PowerSupply");
        productRepository.save(p13);

        Product p14 = new Product();
        p14.setName("Nfortec Sagitta X2 PCIe 5.1 ATX 3.1 850W 80 Plus Gold Full Modular");
        p14.setDescription(
                "Garantiza un suministro eléctrico constante y eficiente gracias a su certificación 80 PLUS Gold, cuidando el rendimiento de tu equipo y reduciendo el consumo energético.");
        p14.setPrice(137.99);
        p14.setStock(9);
        p14.setCategory("PowerSupply");
        productRepository.save(p14);

        // SSD

        Product p15 = new Product();
        p15.setName("Samsung 990 EVO Plus 1TB Disco SSD 7150MB/s NVME PCIe 4.0 NVMe 2.0 NAND");
        p15.setDescription(
                "Realice las tareas más rápido. El 990 EVO Plus con la última NAND ofrece velocidades de lectura/escritura secuencial mejoradas de hasta 7150/6300 MB/s. Archivos enormes, transferencia instantánea.");
        p15.setPrice(174.95);
        p15.setStock(23);
        p15.setCategory("SSD");
        productRepository.save(p15);

        Product p16 = new Product();
        p16.setName("WD BLACK SN7100 2TB Disco SSD 7250MB/s NVMe PCIe 4.0 M.2 Gen4 TLC 3D NAND");
        p16.setDescription(
                "Proporciona hasta 7250 MB/s de velocidad de lectura y 6900 MB/s de velocidad de escritura (modelos de 1-2 TB), lo que permite un aumento del rendimiento de hasta un 35 % en comparación con los SSD de la generación anterior.");
        p16.setPrice(269.95);
        p16.setStock(11);
        p16.setCategory("SSD");
        productRepository.save(p16);

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
        r2.setProduct(p7);
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