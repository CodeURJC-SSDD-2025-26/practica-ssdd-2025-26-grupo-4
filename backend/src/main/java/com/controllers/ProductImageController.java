package com.controllers;

import com.example.backend.models.Product;
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

    @GetMapping("/product/{id}/image")
    public ResponseEntity<Object> downloadImage(@PathVariable long id) throws SQLException {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent() && product.get().getImageFile() != null) {
            Resource file = new InputStreamResource(product.get().getImageFile().getBinaryStream());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}