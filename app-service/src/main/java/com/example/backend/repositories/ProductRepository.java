package com.example.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // Methods to filter out soft-deleted products
        List<Product> findByActiveTrue();

        List<Product> findByCategoryAndActiveTrue(String category);

        // Custom query filtering only active products
        @Query("SELECT p FROM Product p WHERE p.active = true AND " +
                        "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
                        "(:category IS NULL OR p.category = :category) AND " +
                        "(:brand IS NULL OR p.brand = :brand) AND " +
                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR p.price <= :maxPrice)")
        Page<Product> findWithFilters(@Param("name") String name,
                        @Param("category") String category,
                        @Param("brand") String brand,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        Pageable pageable);

}