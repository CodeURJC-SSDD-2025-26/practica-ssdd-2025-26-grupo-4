package com.example.utility_service.model;

import jakarta.validation.constraints.NotBlank;

public class PdfRequest {

    @NotBlank(message = "El título del PDF es obligatorio")
    private String title;

    @NotBlank(message = "El contenido del PDF es obligatorio")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
