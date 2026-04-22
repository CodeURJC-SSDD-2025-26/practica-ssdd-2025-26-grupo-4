package com.example.backend.services;

import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;

import com.example.backend.models.Order;
import com.example.backend.models.Product;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.mail.internet.MimeMessage;

import com.lowagie.text.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Generates the PDF content for the invoice.
     * Prevents "null" strings by providing fallback values.
     */
    public byte[] generatePdfInvoice(Order order) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Header
        document.add(new Paragraph("PCBuilderShop - INVOICE"));
        document.add(new Paragraph(" "));
        
        // Order information
        document.add(new Paragraph("Order ID: " + (order.getId() != null ? order.getId() : "N/A")));
        document.add(new Paragraph("Date: " + (order.getFormattedDate() != null && !order.getFormattedDate().isEmpty() ? order.getFormattedDate() : "N/A")));
        document.add(new Paragraph("Status: " + (order.getStatus() != null ? order.getStatus() : "N/A")));
        document.add(new Paragraph(" "));

        // Customer information
        String customerName = "Customer";
        String customerEmail = "N/A";
        if (order.getUser() != null) {
            if (order.getUser().getDisplayName() != null && !order.getUser().getDisplayName().isEmpty()) {
                customerName = order.getUser().getDisplayName();
            } else if (order.getUser().getUsername() != null) {
                customerName = order.getUser().getUsername();
            }
            if (order.getUser().getEmail() != null) {
                customerEmail = order.getUser().getEmail();
            }
        }
        document.add(new Paragraph("Customer: " + customerName));
        document.add(new Paragraph("Email: " + customerEmail));
        document.add(new Paragraph(" "));

        // Shipping address
        String address = (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) 
            ? order.getShippingAddress() : "Not specified";
        String city = (order.getCity() != null && !order.getCity().isEmpty()) 
            ? order.getCity() : "";
        String postalCode = (order.getPostalCode() != null && !order.getPostalCode().isEmpty()) 
            ? order.getPostalCode() : "";
        String country = (order.getCountry() != null && !order.getCountry().isEmpty()) 
            ? order.getCountry() : "";
        document.add(new Paragraph("Shipping Address: " + address));
        if (!city.isEmpty() || !postalCode.isEmpty() || !country.isEmpty()) {
            document.add(new Paragraph(city + " " + postalCode + ", " + country));
        }
        document.add(new Paragraph(" "));

        // Products Table
        Table table = new Table(3);
        table.addCell(" Product\n");
        table.addCell(" Quantity\n");
        table.addCell("  Price\n");

        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (Product p : order.getProducts()) {
                table.addCell(p.getName() != null ? p.getName() : "Unknown");
                table.addCell("1");
                table.addCell(String.format("%.2f€", p.getPrice()));
            }
        }
        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("TOTAL: " + String.format("%.2f€", order.getTotalPrice())));

        document.close();
        return out.toByteArray();
    }

    /**
     * Sends the invoice via email. 
     * Now sends synchronously to ensure completion.
     */
    public void sendInvoiceEmail(Order order) {
        try {
            logger.info("Starting to send invoice email for order ID: {}", order.getId());
            
            if (order.getUser() == null || order.getUser().getEmail() == null) {
                logger.error("Cannot send email: User or email is null for order ID: {}", order.getId());
                return;
            }

            logger.info("Sending email to: {}", order.getUser().getEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Factura de tu pedido #" + order.getId());
            helper.setFrom("noreply@pcbuildershop.com");

            // Greeting logic to avoid "Hello null"
            String name = (order.getUser().getFirstName() != null && !order.getUser().getFirstName().isEmpty()) 
                ? order.getUser().getFirstName() 
                : order.getUser().getUsername();
            
            String body = "Hola " + name + ",\n\n" +
                          "Te adjuntamos la factura de tu compra reciente.\n\n" +
                          "Detalles del Pedido:\n" +
                          "ID del Pedido: " + order.getId() + "\n" +
                          "Fecha: " + order.getFormattedDate() + "\n" +
                          "Total: " + String.format("%.2f", order.getTotalPrice()) + "€\n\n" +
                          "¡Gracias por elegir PCBuilderShop!\n\n" +
                          "Saludos cordiales,\nEl equipo de PCBuilderShop";
            
            helper.setText(body);

            byte[] pdfBytes = generatePdfInvoice(order);
            helper.addAttachment("Factura" + order.getId() + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            logger.info("Invoice email sent successfully for order ID: {} to {}", order.getId(), order.getUser().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send invoice email for order ID: {}", order.getId(), e);
        }
    }
}