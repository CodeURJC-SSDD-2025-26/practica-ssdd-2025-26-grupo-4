package com.controllers;

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
    @GetMapping("/admin/admin-dashboard")
    public String adminDashboard(){
        return "pages/admin/admin-dashboard";
    }
    @GetMapping("/admin/item-create")
    public String itemCreate(){
        return "pages/admin/item-create";
    }
    @GetMapping("/admin/item-edit")
    public String itemEdit(){
        return "pages/admin/item-edit";
    }
    @GetMapping("/admin/item-list")
    public String itemList(){
        return "pages/admin/item-list";
    }
    @GetMapping("/admin/order-list")
    public String orderList(){
        return "pages/admin/order-list";
    }
    @GetMapping("/admin/order-edit")
    public String orderEdit(){
        return "pages/admin/order-edit";
    }
    @GetMapping("/admin/order-management")
    public String orderManagement(){
        return "pages/admin/order-management";
    }
    @GetMapping("/admin/review-list")
    public String reviewList(){
        return "pages/admin/review-list";
    }
    @GetMapping("/admin/user-create")
    public String userCreate(){
        return "pages/admin/user-create";
    }
    @GetMapping("/admin/user-edit")
    public String userEdit(){
        return "pages/admin/user-edit";
    }
    @GetMapping("/admin/user-list")
    public String userList(){
        return "pages/admin/user-list";
    }
    
}
