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

import java.security.Principal;
import java.util.Map;
import java.util.Arrays;
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
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> data) {
        try {
            User newUser = userService.registerNewUser(data.get("username"), data.get("email"), data.get("password"));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(newUser));
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

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(newUser));
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

            return ResponseEntity.status(HttpStatus.CREATED).body("Address added successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new Exception("User not found"));

            user.setUsername(userData.get("username").toString());
            user.setEmail(userData.get("email").toString());
            user.setEncodedPassword(passwordEncoder.encode(userData.get("password").toString()));
            user.setRoles(Arrays.asList(
                    userData.get("admin") != null && (Boolean) userData.get("admin") ? "ROLE_ADMIN" : "ROLE_USER"));

            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> dtos = userService.findAll(pageable).map(this::convertToDTO);
        return ResponseEntity.ok(dtos);
    }
}