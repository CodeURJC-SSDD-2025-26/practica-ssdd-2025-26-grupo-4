package com.example.backend.controllers;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Importamos el servicio
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.backend.models.Product;
import com.example.backend.models.ProductImage;
import com.example.backend.services.ProductService;

@Controller
public class ProductImageController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/{id}/image")
    public ResponseEntity<Object> downloadMainImage(@PathVariable long id) throws SQLException {
        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent() && product.get().getImageFile() != null) {
            Resource file = new InputStreamResource(product.get().getImageFile().getBinaryStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/product/image/{imageId}")
    public ResponseEntity<Object> downloadSpecificImage(@PathVariable long imageId) throws SQLException {
        Optional<ProductImage> image = productService.getProductImageById(imageId);

        if (image.isPresent() && image.get().getImageFile() != null) {
            Resource file = new InputStreamResource(image.get().getImageFile().getBinaryStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }
}