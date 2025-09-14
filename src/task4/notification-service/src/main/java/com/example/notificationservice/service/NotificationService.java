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

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void handleUserEvent(UserEvent event) {
        log.info("Received user event: type={}, email={}", event.eventType(), event.email());
        try {
            switch (event.eventType()) {
                case "USER_CREATED" -> {
                    log.debug("Handling USER_CREATED for {}", event.email());
                    sendWelcomeEmail(event.email());
                    log.info("Welcome email queued/sent to {}", event.email());
                }
                case "USER_DELETED" -> {
                    log.debug("Handling USER_DELETED for {}", event.email());
                    sendAccountDeletedEmail(event.email());
                    log.info("Account deletion email queued/sent to {}", event.email());
                }
                default -> {
                    log.warn("Unknown event type received: {} for {}", event.eventType(), event.email());
                }
            }
        } catch (NotificationException e) {
            log.error("Notification processing failed for type={} email={}: {}", event.eventType(), event.email(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while processing event type={} email={}", event.eventType(), event.email(), e);
            throw new NotificationException("Unexpected error processing event", e);
        }
    }

    public void sendEmail(String to, String subject, String text) {
        log.debug("Preparing email: to={}, subjectLength={}", to, subject != null ? subject.length() : 0);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
            log.info("Email sent: to={}, subject='{}'", to, subject);
        } catch (MessagingException e) {
            String errorMessage = String.format("Failed to send email to: %s", to);
            log.error("{}; cause={}", errorMessage, e.getMessage(), e);
            throw new NotificationException(errorMessage, e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage(), e);
            throw new NotificationException("Unexpected error while sending email", e);
        }
    }

    private void sendWelcomeEmail(String email) {
        log.debug("Composing welcome email for {}", email);
        sendEmail(email, "Добро пожаловать!", "Здравствуйте! Ваш аккаунт на сайте был успешно создан.");
    }

    private void sendAccountDeletedEmail(String email) {
        log.debug("Composing account deletion email for {}", email);
        sendEmail(email, "Аккаунт удалён", "Здравствуйте! Ваш аккаунт был удалён.");
    }
}
