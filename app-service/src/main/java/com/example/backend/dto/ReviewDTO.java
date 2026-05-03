package com.example.backend.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long id;
    private int score;
    private String comment;
    private String date;
    private String username; 
    private String adminReply;
}