package com.example.backend.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.backend.models.Product;
import com.example.backend.services.MainService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        boolean isLoggedIn = principal != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // Productos (hardware news)
        List<Product> hardwareNews = mainService.getLatestProducts();
        model.addAttribute("productos", hardwareNews);

        // Recomendaciones
        List<Product> recommendations;

        if (isLoggedIn) {
            recommendations = mainService.getRecommendations(principal.getName());
        } else {
            recommendations = hardwareNews;
        }

        model.addAttribute("recomendados", recommendations);
        model.addAttribute("hasRecommendations",
                mainService.hasRecommendations(recommendations));

        // CSRF
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            model.addAttribute("_csrf", token);
        }

        return "index";
    }

    @GetMapping("/index")
    public String indexAlt(Model model) {
        model.addAttribute("productos", mainService.getAllProducts());
        return "index";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error-403";
    }
}