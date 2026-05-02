package com.example.backend.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Muro de contención: Solo admins pasan de aquí (✧ω✧)
public class AdminRestController {

    // TODO: @Autowired private AdminService adminService; (o los services que haga tu compi)

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        // TODO: Devolver el AdminDashboardDTO
        return ResponseEntity.ok(Map.of(
            "totalProductos", 150,
            "totalUsuarios", 42,
            "totalIngresos", "15420,50",
            "mensaje", "Datos falsos de dashboard"
        ));
    }

    // --- GESTIÓN DE PRODUCTOS ---

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(
            @RequestPart("data") Map<String, Object> productRequestDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        // TODO: Llamar al service para crear el producto con su imagen
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Producto creado de mentira"));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> editProduct(
            @PathVariable Long id,
            @RequestPart("data") Map<String, Object> productRequestDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        // TODO: Llamar al service para actualizar
        return ResponseEntity.ok(Map.of("mensaje", "Producto " + id + " editado de mentira"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        // TODO: Hacer el soft-delete (active = false) en el service
        return ResponseEntity.ok(Map.of("mensaje", "Producto " + id + " desactivado"));
    }

    // --- GESTIÓN DE USUARIOS ---

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userRequestDto) {
        // TODO: Crear usuario desde el admin
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Usuario creado por admin"));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @RequestBody Map<String, Object> userRequestDto) {
        // TODO: Editar usuario
        return ResponseEntity.ok(Map.of("mensaje", "Usuario " + id + " editado"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // TODO: Borrar usuario
        return ResponseEntity.noContent().build();
    }

    // --- GESTIÓN DE PEDIDOS ---

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        // TODO: Actualizar estado del pedido
        return ResponseEntity.ok(Map.of("mensaje", "Estado del pedido " + id + " actualizado a " + statusUpdate.get("status")));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    // --- GESTIÓN DE RESEÑAS ---

    @PostMapping("/reviews/{id}/reply")
    public ResponseEntity<?> replyToReview(@PathVariable Long id, @RequestBody Map<String, String> replyData) {
        // TODO: Añadir respuesta del admin a la reseña
        return ResponseEntity.ok(Map.of("mensaje", "Respuesta añadida a la reseña " + id));
    }

    @DeleteMapping("/reviews/{id}/reply")
    public ResponseEntity<?> deleteReviewReply(@PathVariable Long id) {
        // TODO: Borrar la respuesta del admin
        return ResponseEntity.ok(Map.of("mensaje", "Respuesta de la reseña " + id + " eliminada"));
    }
}