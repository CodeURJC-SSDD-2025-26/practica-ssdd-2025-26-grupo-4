package com.example.backend.services;

import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.backend.models.Order;
import com.example.backend.models.Product;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String utilityServiceUrl;

    public EmailService(@Value("${utility.service.url:http://localhost:8080/api/utility}") String utilityServiceUrl) {
        this.utilityServiceUrl = utilityServiceUrl;
        logger.info("EmailService initialized with utility-service URL: {}", utilityServiceUrl);
    }

    /**
     * Builds the invoice document using the remote utility-service PDF endpoint.
     */
    public byte[] generatePdfInvoice(Order order) throws Exception {
        try {
            PdfApiRequest request = new PdfApiRequest(
                    "Factura pedido #" + (order.getId() != null ? order.getId() : "N/A"),
                    buildInvoiceText(order));
            byte[] response = restTemplate.postForObject(utilityServiceUrl + "/pdf", request, byte[].class);
            if (response == null) {
                throw new IllegalStateException("Utility service returned empty PDF response");
            }
            return response;
        } catch (RestClientException e) {
            throw new Exception("Failed to generate PDF via utility-service", e);
        }
    }

    /**
     * Sends the invoice email through utility-service.
     * Catches errors internally without throwing to allow payment flow to complete.
     */
    public void sendInvoiceEmail(Order order) {
        try {
            sendInvoiceEmailInternal(order);
        } catch (Exception e) {
            logger.error("Failed to send invoice email for order ID: {}", 
                    order != null ? order.getId() : "unknown", e);
        }
    }

    /**
     * Internal method that throws exceptions for explicit error handling.
     * Use this when you need to know if email sending failed.
     */
    public void sendInvoiceEmailInternal(Order order) throws Exception {
        if (order.getUser() == null || order.getUser().getEmail() == null) {
            throw new IllegalArgumentException("Cannot send email: User or email is null for order ID: " + order.getId());
        }

        String recipient = order.getUser().getEmail();
        logger.info("Sending invoice email to {} for order ID: {} via utility-service URL: {}", 
                recipient, order.getId(), utilityServiceUrl);

        byte[] pdfBytes = generatePdfInvoice(order);

        EmailApiRequest request = new EmailApiRequest(
                recipient,
                "Factura de tu pedido #" + order.getId(),
                buildEmailBody(order),
                "noreply@pcbuildershop.com",
                "Factura_" + order.getId() + ".pdf",
                Base64.getEncoder().encodeToString(pdfBytes));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(utilityServiceUrl + "/email", request, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Email sent successfully for order ID: {}", order.getId());
            } else {
                throw new Exception("Utility-service returned status " + response.getStatusCode() + 
                        " for order ID: " + order.getId());
            }
        } catch (RestClientException e) {
            throw new Exception("Failed to connect to utility-service at " + utilityServiceUrl + 
                    " for order ID: " + order.getId(), e);
        }
    }

    private String buildEmailBody(Order order) {
        String name = "cliente";
        if (order.getUser() != null) {
            if (order.getUser().getFirstName() != null && !order.getUser().getFirstName().isBlank()) {
                name = order.getUser().getFirstName();
            } else if (order.getUser().getUsername() != null) {
                name = order.getUser().getUsername();
            }
        }

        return "Hola " + name + ",\n\n" +
                "Gracias por tu compra en PCBuilderShop. Adjuntamos los detalles de tu pedido y la factura.\n\n" +
                "ID del Pedido: " + order.getId() + "\n" +
                "Fecha: " + (order.getFormattedDate() != null ? order.getFormattedDate() : "N/A") + "\n" +
                "Total: " + String.format("%.2f", order.getTotalPrice()) + "€\n\n" +
                "Si necesitas ayuda, responde a este email.\n\n" +
                "Saludos cordiales,\nEl equipo de PCBuilderShop";
    }

    private String buildInvoiceText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("PCBuilderShop - FACTURA\n\n");
        sb.append("Pedido #: ").append(order.getId() != null ? order.getId() : "N/A").append("\n");
        sb.append("Fecha: ").append(order.getFormattedDate() != null ? order.getFormattedDate() : "N/A").append("\n");
        sb.append("Estado: ").append(order.getStatus() != null ? order.getStatus() : "N/A").append("\n\n");

        String customerName = "Cliente";
        String customerEmail = "N/A";
        if (order.getUser() != null) {
            if (order.getUser().getDisplayName() != null && !order.getUser().getDisplayName().isBlank()) {
                customerName = order.getUser().getDisplayName();
            } else if (order.getUser().getUsername() != null) {
                customerName = order.getUser().getUsername();
            }
            if (order.getUser().getEmail() != null) {
                customerEmail = order.getUser().getEmail();
            }
        }
        sb.append("Cliente: ").append(customerName).append("\n");
        sb.append("Email: ").append(customerEmail).append("\n\n");

        sb.append("Dirección de envío: ")
                .append(order.getShippingAddress() != null && !order.getShippingAddress().isBlank()
                        ? order.getShippingAddress()
                        : "No especificada")
                .append("\n");
        sb.append(order.getCity() != null ? order.getCity() : "");
        if (order.getPostalCode() != null && !order.getPostalCode().isBlank()) {
            sb.append(" ").append(order.getPostalCode());
        }
        if (order.getCountry() != null && !order.getCountry().isBlank()) {
            sb.append(", ").append(order.getCountry());
        }
        sb.append("\n\n");

        sb.append("Productos:\n");
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (Product p : order.getProducts()) {
                sb.append("- ")
                        .append(p.getName() != null ? p.getName() : "Desconocido")
                        .append(" (\"")
                        .append(String.format("%.2f€", p.getPrice()))
                        .append("\")\n");
            }
        } else {
            sb.append("No hay productos en el pedido.\n");
        }

        sb.append("\nTOTAL: ").append(String.format("%.2f€", order.getTotalPrice())).append("\n");
        return sb.toString();
    }

    private static record EmailApiRequest(String to, String subject, String body, String from,
            String attachmentName, String attachmentBase64) {
    }

    private static record PdfApiRequest(String title, String content) {
    }
}
