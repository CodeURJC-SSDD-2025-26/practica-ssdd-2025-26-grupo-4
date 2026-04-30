package com.example.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.backend.models.Product;
import com.example.backend.repositories.ProductRepository;
import com.example.backend.repositories.ReviewRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/item-detail")
    public String itemDetail(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            productRepository.findById(id).ifPresent(product -> {
                model.addAttribute("producto", product);
                model.addAttribute("reviews", reviewRepository.findByProductId(id));
            });
        }
        return "pages/item-detail";
    }

    @GetMapping("/search-result")
    public String searchResult(@RequestParam(value = "category", required = false) String category, Model model) {
        if (category != null && !category.isBlank()) {
            model.addAttribute("productos", productRepository.findByCategory(category));
            model.addAttribute("categoryName", category);
        } else {
            model.addAttribute("productos", productRepository.findAll());
        }
        return "pages/search-result";
    }

    @GetMapping("/search")
    public String searchProducts(Model model,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            HttpServletRequest request) {

        try {
            // Lógica de limpieza de nulos
            String searchName = (name != null && !name.trim().isEmpty()) ? name.trim().toLowerCase() : null;
            String searchCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
            String searchBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : null;

            // Procesamiento inteligente de la búsqueda (ej: "procesadores intel")
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
                    searchName = searchName
                            .replaceAll("refrigeración|refrigeracion|cooling|ventiladores?|disipadores?", "").trim();
                }

                if (searchName.isEmpty()) {
                    searchName = null;
                }
            }

            // Lógica de ordenación
            Sort sortOrder = Sort.unsorted();
            if ("priceAsc".equals(sort)) {
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
            } else if ("priceDesc".equals(sort)) {
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
            }

            List<Product> results = productRepository.findWithFilters(searchName, searchCategory, searchBrand, minPrice,
                    maxPrice, sortOrder);

            model.addAttribute("productos", results);
            model.addAttribute("query", name != null ? name : "");
            model.addAttribute("category", category != null ? category : "");
            model.addAttribute("brand", brand != null ? brand : "");
            model.addAttribute("currentSort", sort != null ? sort : "");

            if (name != null && !name.trim().isEmpty()) {
                model.addAttribute("searchTerm", name);
            } else {
                model.addAttribute("searchTerm", null);
            }

            model.addAttribute("isLoggedIn", request.getUserPrincipal() != null);
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
            if (token != null) {
                model.addAttribute("_csrf", token);
            }

            return "pages/search-result";
        } catch (Exception e) {
            return "redirect:/?error=true";
        }
    }
}
