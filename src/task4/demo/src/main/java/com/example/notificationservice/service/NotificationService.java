package com.example.notificationservice.service;

import com.example.notificationservice.dto.UserEvent;
import com.example.notificationservice.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;

    @KafkaListener(topics = "user-events")
    public void handleUserEvent(UserEvent event) {
        try {
            switch (event.eventType()) {
                case "USER_CREATED":
                    sendWelcomeEmail(event.email());
                    break;
                case "USER_DELETED":
                    sendAccountDeletedEmail(event.email());
                    break;
                default:
                    log.warn("Unknown event type: {}", event.eventType());
            }
        } catch (NotificationException e) {
            log.error("Failed to process event: {}", event.eventType(), e);
            throw e;
        }
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            String errorMessage = String.format("Failed to send email to: %s", to);
            log.error(errorMessage, e);
            throw new NotificationException(errorMessage, e);
        }
    }

    private void sendWelcomeEmail(String email) {
        sendEmail(email, "Добро пожаловать!",
                "Здравствуйте! Ваш аккаунт на сайте был успешно создан.");
    }

    private void sendAccountDeletedEmail(String email) {
        sendEmail(email, "Аккаунт удалён",
                "Здравствуйте! Ваш аккаунт был удалён.");
    }
}