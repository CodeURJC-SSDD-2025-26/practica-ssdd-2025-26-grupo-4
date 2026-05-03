package com.example.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private List<String> roles;
    private boolean hasPicture;
    private String roleLabel;
}