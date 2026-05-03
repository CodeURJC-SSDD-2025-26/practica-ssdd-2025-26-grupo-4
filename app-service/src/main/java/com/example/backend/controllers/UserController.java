package com.example.backend.controllers;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.services.OrderService;
import com.example.backend.services.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService; 

    @GetMapping({"/user-registration", "/user_registration"})
    public String userRegistration() {
        return "pages/user_registration";
    }

    @PostMapping("/user-registration")
    public String registerUser(@RequestParam String username, @RequestParam String email,
            @RequestParam String password, @RequestParam String repeatPassword, Model model) {

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "pages/user_registration";
        }

        if (!email.contains("@")) {
            model.addAttribute("error", "Email inválido.");
            return "pages/user_registration";
        }

        if (!password.equals(repeatPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "pages/user_registration";
        }

        if (userService.existsByUsername(username)) {
            model.addAttribute("error", "El usuario ya existe.");
            return "pages/user_registration";
        }

        userService.registerNewUser(username, email, password);
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal != null) {
            userService.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("usuario", user);
                // Usamos los servicios correspondientes para obtener datos relacionados
                model.addAttribute("pedidos", user.getOrders()); 
                model.addAttribute("reviews", user.getReviews());
            });
        }
        return "pages/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username, @RequestParam String email,
            @RequestParam(required = false) MultipartFile imageFile, Principal principal) throws IOException {
        
        if (principal != null) {
            String oldUsername = principal.getName();
            userService.updateProfile(oldUsername, username, email, imageFile);

            // Actualizar la sesión si el username ha cambiado
            if (!oldUsername.equals(username)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, auth.getCredentials(), auth.getAuthorities())
                );
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/address/delete")
    public String deleteAddress(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (userService.deleteAddressWithCheck(id, principal.getName(), isAdmin)) {
            return "redirect:/payment";
        }
        return "redirect:/error/403";
    }
}