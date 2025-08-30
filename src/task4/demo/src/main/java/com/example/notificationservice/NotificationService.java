package com.example.notificationservice;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;

    @KafkaListener(topics = "user-events")
    public void handleUserEvent(UserEvent event) {
        try {
            switch (event.eventType()) {
                case "USER_CREATED":
                    sendEmail(event.email(), "Добро пожаловать!",
                            "Здравствуйте! Ваш аккаунт на сайте был успешно создан.");
                    break;
                case "USER_DELETED":
                    sendEmail(event.email(), "Аккаунт удалён",
                            "Здравствуйте! Ваш аккаунт был удалён.");
                    break;
                default:
                    System.out.println("Unknown event type: " + event.eventType());
            }
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);

        mailSender.send(message);
    }
}
