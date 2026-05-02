package com.example.backend.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    // TODO: @Autowired private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registrationData) {
        // TODO: Validar contraseñas y crear usuario. Cuidado con el EmailService aquí, que lo llame el Service.
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Usuario registrado. ¡Bienvenido!"));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getProfile(Principal principal) {
        // TODO: Devolver UserProfileDTO con sus pedidos y reviews
        return ResponseEntity.ok(Map.of("mensaje", "Perfil de " + principal.getName()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") Map<String, Object> updateData,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            Principal principal) {
        // TODO: Actualizar perfil del usuario
        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado con éxito"));
    }

    @DeleteMapping("/address/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Principal principal) {
        // TODO: Comprobar que la dirección es suya y borrarla
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<?> getUserImage(@PathVariable Long id) {
        // TODO: Devolver el BLOB de la foto de perfil
        return ResponseEntity.notFound().build(); // Placeholder
    }
}