package com.example.utility_service;

import com.example.utility_service.model.PdfRequest;
import com.example.utility_service.service.PdfService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTests {

    @Test
    void generatePdfReturnsBytes() {
        PdfService pdfService = new PdfService();
        PdfRequest request = new PdfRequest();
        request.setTitle("Prueba PDF");
        request.setContent("Este PDF se genera con el servicio utility-service sin necesidad de base de datos.");

        byte[] result = pdfService.generatePdf(request);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
