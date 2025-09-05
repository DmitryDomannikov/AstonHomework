package com.example.customnotification.integration;

import com.example.notificationservice.dto.EmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(KafkaConfigurationTest.TestConfig.class)
class NotificationControllerIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            JavaMailSender mockMailSender = org.mockito.Mockito.mock(JavaMailSender.class);
            doNothing().when(mockMailSender).send(any(SimpleMailMessage.class));
            return mockMailSender;
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void shouldSendManualEmail() {
        // Given
        EmailRequest request = new EmailRequest(
                "test@example.com",
                "Test Subject",
                "Test Message"
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/notifications/email",
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());

        // Проверяем что метод send был вызван с SimpleMailMessage
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}