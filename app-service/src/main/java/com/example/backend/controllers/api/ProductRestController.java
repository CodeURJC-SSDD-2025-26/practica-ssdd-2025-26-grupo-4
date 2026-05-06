package com.example.backend.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.ProductDTO;
import com.example.backend.models.Product;
import com.example.backend.services.ProductService;
import com.example.backend.services.RecommendationService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ProductRestController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ProductService productService;

    private ProductDTO convertToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setCategory(p.getCategory());
        dto.setStock(p.getStock());
        dto.setActive(p.isActive());
        return dto;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            Pageable pageable) {

        Page<Product> products = productService.advancedSearch(name, category, brand, minPrice, maxPrice, sort,
                pageable);

        Page<ProductDTO> dtos = products.map(this::convertToDTO);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        return productService.getProductById(id).map(product -> {
            ProductDTO dto = convertToDTO(product);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("message", "Generic recommendations for guests."));
        }
        return ResponseEntity.ok(Map.of("message", "Personalized recommendations for " + principal.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> productData) {
        try {
            Product product = new Product();

            product.setName(productData.get("name").toString());
            product.setDescription(productData.get("description").toString());
            product.setPrice(Double.parseDouble(productData.get("price").toString()));
            product.setCategory(productData.get("category").toString());
            product.setStock(Integer.parseInt(productData.get("stock").toString()));
            product.setActive(true);

            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getMainImage(@PathVariable Long id) {
        return ResponseEntity.notFound().build();
    }
}