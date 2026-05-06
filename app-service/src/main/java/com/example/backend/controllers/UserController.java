package com.example.backend.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.models.Address;
import com.example.backend.models.User;
import com.example.backend.services.OrderService;
import com.example.backend.services.ReviewService;
import com.example.backend.services.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user-registration")
    public String userRegistration() {
        return "pages/user_registration";
    }

    @GetMapping("/user_registration")
    public String userRegistration2() {
        return "pages/user_registration";
    }

    @PostMapping("/user-registration")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String repeatPassword,
            Model model) {

        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "pages/user_registration"; //
        }

        if (!email.contains("@") || !email.contains(".")) {
            model.addAttribute("error", "Por favor, introduce un correo electrónico válido.");
            return "pages/user_registration";
        }

        if (!password.equals(repeatPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "pages/user_registration";
        }

        if (userService.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El usuario ya existe.");
            return "pages/user_registration";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        user.setRoles(Arrays.asList("ROLE_USER"));

        userService.saveUser(user);

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal != null) {
            userService.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("usuario", user);
                model.addAttribute("reviews", reviewService.getReviewsByUserId(user.getId()));
                model.addAttribute("pedidos", orderService.getOrdersByUserId(user.getId()));
            });
        }
        return "pages/profile";
    }

    // --- PROFILE UPDATE ---

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) MultipartFile imageFile,
            Principal principal) throws IOException {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                User user = optUser.get();
                user.setUsername(username);
                user.setEmail(email);

                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        user.setProfilePicture(
                                BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                        user.setHasPicture(true);
                    } catch (IOException e) {
                        // Ignore image upload errors
                    }
                }

                userService.saveUser(user);

                // Re-authenticate with the new username so the SecurityContext is up-to-date
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        username, currentAuth.getCredentials(), currentAuth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        }
        return "redirect:/profile";
    }

    // Allow users (owner) or admins to delete/edit their own addresses
    @PostMapping("/address/delete")
    public String deleteAddress(@RequestParam Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            userService.deleteAddress(id, principal.getName(), isAdmin);
            return "redirect:/payment";
        } catch (Exception e) {
            return "redirect:/error/403";
        }
    }

}
