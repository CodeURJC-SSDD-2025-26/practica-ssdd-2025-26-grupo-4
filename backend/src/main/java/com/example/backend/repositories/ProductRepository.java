package com.example.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.models.Product;
import com.example.backend.models.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT i FROM ProductImage i WHERE i.id = :imageId")
    Optional<ProductImage> findImageById(@Param("imageId") Long imageId);

    List<Product> findByCategory(String category);
}
