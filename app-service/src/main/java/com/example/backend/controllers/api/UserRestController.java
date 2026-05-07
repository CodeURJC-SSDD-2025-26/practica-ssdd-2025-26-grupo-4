package com.example.backend.controllers.api;

import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.services.OrderService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.example.backend.dto.UserRegisterRequest;

import java.security.Principal;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.URI;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDTO convertToDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setRoles(u.getRoles());
        dto.setHasPicture(u.isHasPicture());
        return dto;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest data, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", result.getFieldError().getDefaultMessage()));
        }

        try {
            User newUser = userService.registerNewUser(data.getUsername(), data.getEmail(), data.getPassword());
            URI location = URI.create("/api/v1/users/" + newUser.getId());
            return ResponseEntity.created(location).body(convertToDTO(newUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdminOrUser(@RequestBody Map<String, Object> data) {
        try {
            boolean isAdmin = data.containsKey("admin") && Boolean.parseBoolean(data.get("isAdmin").toString());

            User newUser = userService.createAdminOrUser(
                    data.get("username").toString(),
                    data.get("email").toString(),
                    data.get("password").toString(),
                    isAdmin);

            URI location = URI.create("/api/v1/users/" + newUser.getId());
            return ResponseEntity.created(location).body(convertToDTO(newUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getProfile(Principal principal) {
        return userService.findByUsername(principal.getName())
                .map(user -> ResponseEntity.ok(convertToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetail(@PathVariable Long id) {
        return userService.findById(id).map(user -> {
            UserDTO dto = convertToDTO(user);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/address")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addAddress(@RequestBody Map<String, String> addressData, Principal principal) {
        try {
            orderService.addUserAddress(
                    principal.getName(),
                    addressData.get("street"),
                    addressData.get("city"),
                    addressData.get("postalCode"),
                    addressData.get("country"));

            URI location = URI.create("/api/v1/users/profile");
            return ResponseEntity.created(location).body(Map.of("message", "Dirección añadida correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            if (userData.containsKey("username") && userData.get("username") != null) {
                user.setUsername(userData.get("username").toString());
            }

            if (userData.containsKey("email") && userData.get("email") != null) {
                user.setEmail(userData.get("email").toString());
            }

            if (userData.containsKey("password") && userData.get("password") != null) {
                String rawPassword = userData.get("password").toString();
                if (!rawPassword.trim().isEmpty()) {
                    user.setEncodedPassword(passwordEncoder.encode(rawPassword));
                }
            }

            if (userData.containsKey("admin") && userData.get("admin") != null) {
                boolean isAdmin = Boolean.parseBoolean(userData.get("admin").toString());
                user.setRoles(new ArrayList<>(Arrays.asList(isAdmin ? "ROLE_ADMIN" : "ROLE_USER")));
            }

            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(convertToDTO(updatedUser));

        } catch (Exception e) {
            System.out.println("Error para el usuario: " + id);
            e.printStackTrace(); 
            String mensaje = e.getMessage() != null ? e.getMessage() : "Excepción sin mensaje: " + e.getClass().getSimpleName();
            return ResponseEntity.badRequest().body(Map.of("error", mensaje));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/address/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Principal principal,
            @RequestHeader(value = "Role", defaultValue = "USER") String role) {
        try {
            boolean isAdmin = role.contains("ADMIN");
            userService.deleteAddress(id, principal.getName(), isAdmin);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<UserDTO> dtos = userService.findAll(pageable).map(this::convertToDTO);
        return ResponseEntity.ok(dtos);
    }
}