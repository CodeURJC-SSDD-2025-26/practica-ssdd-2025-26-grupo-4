package com.example.utility_service.service;

import com.example.utility_service.model.EmailRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.Base64;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String defaultFromAddress;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${spring.mail.username:}") String defaultFromAddress) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.defaultFromAddress = defaultFromAddress;
    }

    public void sendEmail(EmailRequest request) {
        if (mailSender == null) {
            throw new IllegalStateException("El servicio de correo no está configurado. Configure spring.mail.host para habilitar el envío de emails.");
        }

        String fromAddress = StringUtils.hasText(request.getFrom()) ? request.getFrom() : defaultFromAddress;
        if (!StringUtils.hasText(fromAddress)) {
            throw new IllegalArgumentException("La dirección de origen es obligatoria si no se ha configurado spring.mail.username");
        }

        if (StringUtils.hasText(request.getAttachmentName()) || StringUtils.hasText(request.getAttachmentBase64())) {
            if (!StringUtils.hasText(request.getAttachmentName()) || !StringUtils.hasText(request.getAttachmentBase64())) {
                throw new IllegalArgumentException("Si se proporciona un adjunto, attachmentName y attachmentBase64 son obligatorios.");
            }
            sendEmailWithAttachment(request, fromAddress);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new IllegalStateException("No se pudo enviar el correo electrónico: " + exception.getMessage(), exception);
        }
    }

    private void sendEmailWithAttachment(EmailRequest request, String fromAddress) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody());

            // Limpiar la cadena Base64 para eliminar caracteres inválidos
            String cleanedBase64 = request.getAttachmentBase64().replaceAll("[^A-Za-z0-9+/=]", "");
            byte[] attachmentBytes = Base64.getDecoder().decode(cleanedBase64);
            helper.addAttachment(request.getAttachmentName(), new ByteArrayResource(attachmentBytes));

            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo enviar el correo electrónico con adjunto: " + exception.getMessage(), exception);
        }
    }
}
