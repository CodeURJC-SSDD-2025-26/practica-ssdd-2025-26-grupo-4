package com.example.utility_service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequest {

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Email(message = "La dirección de destino debe ser un email válido")
    private String to;

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotBlank(message = "El cuerpo del correo es obligatorio")
    private String body;

    @Email(message = "La dirección de origen debe ser un email válido")
    private String from;

    private String attachmentName;
    private String attachmentBase64;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentBase64() {
        return attachmentBase64;
    }

    public void setAttachmentBase64(String attachmentBase64) {
        this.attachmentBase64 = attachmentBase64;
    }
}
