package com.example.backend.controllers.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @GetMapping
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok(Map.of(
            "message", "prueba",
            "status", "200 OK",
            "info", "prueba"
        ));
    }
}