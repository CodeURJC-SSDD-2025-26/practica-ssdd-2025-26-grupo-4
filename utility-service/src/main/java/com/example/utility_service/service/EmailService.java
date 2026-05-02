package com.example.utility_service.service;

import com.example.utility_service.model.EmailRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
