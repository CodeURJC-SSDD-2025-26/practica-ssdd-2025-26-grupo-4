package com.example.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private String orderDate; // Mejor enviarlo formateado
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String city;
    private String postalCode;
    private String country;
    
    // Devolvemos solo la info básica del usuario para la API
    private String username; 
    
    // Usaremos el ProductDTO que creamos antes para evitar recursividad
    private List<ProductDTO> products; 
}