package com.example.backend.controllers;

import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.backend.models.User;
import com.example.backend.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "pages/login";

    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }


    @ModelAttribute("currentUserId")
    public Long currentUserId(Principal principal) {
        if (principal == null)
            return null;
        Optional<User> u = userService.findByUsername(principal.getName());
        return u.map(User::getId).orElse(null);
    }

}
