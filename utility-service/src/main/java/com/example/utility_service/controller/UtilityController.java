package com.example.utility_service.controller;

import com.example.utility_service.model.EmailRequest;
import com.example.utility_service.model.PdfRequest;
import com.example.utility_service.service.EmailService;
import com.example.utility_service.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/utility")
public class UtilityController {

    private final EmailService emailService;
    private final PdfService pdfService;

    public UtilityController(EmailService emailService, PdfService pdfService) {
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendEmail(request);
        return ResponseEntity.ok(Map.of("status", "Email enviado correctamente"));
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(@Valid @RequestBody PdfRequest request) {
        byte[] pdfBytes = pdfService.generatePdf(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-document.pdf")
                .body(pdfBytes);
    }
}
