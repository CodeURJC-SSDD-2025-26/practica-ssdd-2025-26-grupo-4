package com.example.backend.controllers;

import com.example.backend.models.Product;
import com.example.backend.models.ProductImage;
import com.example.backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.SQLException;
import java.util.Optional;

@Controller
public class ProductImageController {

    @Autowired
    private ProductRepository productRepository;

    // Serves the main image of a product for search results and index page
    @GetMapping("/product/{id}/image")
    public ResponseEntity<Object> downloadMainImage(@PathVariable long id) throws SQLException {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent() && product.get().getImageFile() != null) {
            Resource file = new InputStreamResource(product.get().getImageFile().getBinaryStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }

    // Serves a specific image from the product's gallery using its unique ID.
    @GetMapping("/product/image/{imageId}")
    public ResponseEntity<Object> downloadSpecificImage(@PathVariable long imageId) throws SQLException {
        // We use the custom query defined in ProductRepository to fetch the gallery image
        Optional<ProductImage> image = productRepository.findImageById(imageId);

        if (image.isPresent() && image.get().getImageFile() != null) {
            Resource file = new InputStreamResource(image.get().getImageFile().getBinaryStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }
}

/*
{{#images}}
   <img src="/product/image/{{id}}">
{{/images}}
*/