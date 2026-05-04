package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderDate;
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String city;
    private String postalCode;
    private String country;
    private String username; 
    private List<ProductDTO> products; 
}