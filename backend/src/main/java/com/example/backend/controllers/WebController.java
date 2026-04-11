package com.example.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/item-detail")
    public String itemDetail() {
        return "pages/item-detail";
    }

    @GetMapping("/shopping-cart")
    public String shoppingCart() {
        return "pages/shopping-cart";
    }

    @GetMapping("/create-review")
    public String createReview() {
        return "pages/create-review";
    }

    @GetMapping("/search-result")
    public String searchResult() {
        return "pages/search-result";
    }

    @GetMapping("/payment")
    public String payment() {
        return "pages/payment";
    }

    @GetMapping("/payment-correct")
    public String paymentCorrect() {
        return "pages/payment_correct";
    }

    @GetMapping("/user-registration")
    public String userRegistration() {
        return "pages/user_registration";
    }

    @GetMapping("/login")
    public String login() {
        return "pages/login";
    }

    @GetMapping("/profile")
    public String profile() {
        return "pages/profile";
    }
}
