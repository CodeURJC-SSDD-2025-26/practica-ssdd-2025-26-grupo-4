package com.example.backend.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.services.RecommendationService;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    // El RecommendationService lo dejo porque ya lo tienes hecho y funciona (✧ω✧)
    @Autowired
    private RecommendationService recommendationService;

    // TODO: @Autowired private ProductService productService;

    @GetMapping
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {
        // TODO: Pasar los filtros al service y devolver List<ProductDTO>
        return ResponseEntity.ok(Map.of(
            "mensaje", "Aquí va la lista de productos filtrados",
            "filtros_recibidos", name + ", " + category
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        // TODO: Devolver ProductDetailDTO con sus reviews
        return ResponseEntity.ok(Map.of("mensaje", "Detalles del producto " + id));
    }

    @GetMapping("/news")
    public ResponseEntity<?> getHardwareNews() {
        // TODO: Devolver los últimos productos añadidos
        return ResponseEntity.ok(List.of("Noticia 1 falsa", "Noticia 2 falsa"));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("mensaje", "Recomendaciones genéricas para invitados"));
        }
        // TODO: Aquí usarás tu RecommendationService con el usuario logueado
        return ResponseEntity.ok(Map.of("mensaje", "Recomendaciones personalizadas para " + principal.getName()));
    }

    // --- IMÁGENES ---
    
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getMainImage(@PathVariable Long id) {
        // TODO: Lógica para devolver el BLOB de la imagen principal
        return ResponseEntity.notFound().build(); // Placeholder
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<Resource> getGalleryImage(@PathVariable Long imageId) {
        // TODO: Lógica para devolver imágenes de la galería
        return ResponseEntity.notFound().build(); // Placeholder
    }
}