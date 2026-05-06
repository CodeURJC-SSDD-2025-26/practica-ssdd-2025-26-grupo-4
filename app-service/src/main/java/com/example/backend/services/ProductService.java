package com.example.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import com.example.backend.models.Product;
import com.example.backend.models.ProductImage;
import com.example.backend.models.Review;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<ProductImage> getProductImageById(Long imageId) {
        return productRepository.findImageById(imageId);
    }

    public List<Review> getReviewsByProductId(Long id) {
        return reviewRepository.findByProductId(id);
    }

    public List<Product> getProductsByCategory(String category) {
        if (category != null && !category.isBlank()) {
            return productRepository.findByCategoryAndActiveTrue(category);
        }
        return productRepository.findByActiveTrue();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLatestProducts(int limit) {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado"));
        product.setActive(false);
        productRepository.save(product);
    }

    public List<Product> advancedSearch(String name, String category, String brand, Double minPrice, Double maxPrice,
            String sort) {
        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim().toLowerCase() : null;
        String searchCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String searchBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : null;

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
                searchName = searchName.replaceAll("refrigeración|refrigeracion|cooling|ventiladores?|disipadores?", "")
                        .trim();
            }

            if (searchName.isEmpty()) {
                searchName = null;
            }
        }

        Sort sortOrder = Sort.unsorted();
        if ("priceAsc".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDesc".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.DESC, "price");
        }

        Pageable pageable = sortOrder.isSorted()
                ? Pageable.unpaged(sortOrder)
                : Pageable.unpaged();

        return productRepository.findWithFilters(searchName, searchCategory, searchBrand, minPrice, maxPrice,
                pageable).getContent();

    }

    public Page<Product> advancedSearch(String name, String category, String brand, Double minPrice, Double maxPrice,
            String sor, Pageable pageable) {
        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim().toLowerCase() : null;
        String searchCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String searchBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : null;

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
                searchName = searchName.replaceAll("refrigeración|refrigeracion|cooling|ventiladores?|disipadores?", "")
                        .trim();
            }

            if (searchName.isEmpty()) {
                searchName = null;
            }
        }

        Sort sortOrder = pageable.getSort();
        if ("priceAsc".equals(sor)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDesc".equals(sor)) {
            sortOrder = Sort.by(Sort.Direction.DESC, "price");
        }

        Pageable pageableWithSort = sortOrder.isSorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder)
                : pageable;

        return productRepository.findWithFilters(searchName, searchCategory, searchBrand, minPrice, maxPrice,
                pageableWithSort);

    }
}