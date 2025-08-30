package com.example.customnotification.integration;

import com.example.notificationservice.UserEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(KafkaConfigurationTest.TestConfig.class)
class NotificationServiceIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceIntegrationTest.class);

    @Configuration
    static class TestConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            JavaMailSender mockMailSender = org.mockito.Mockito.mock(JavaMailSender.class);
            doNothing().when(mockMailSender).send(any(SimpleMailMessage.class));
            return mockMailSender;
        }
    }

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafka.getBootstrapServers();
        log.info("Configuring Kafka bootstrap servers: {}", bootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);

        // Добавляем настройки для JSON сериализации
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers",
                () -> "false");
    }

    @Test
    void shouldSendEmailOnUserCreatedEvent() throws Exception {
        // Given
        UserEvent event = new UserEvent("USER_CREATED", "test@example.com", 1L);
        log.debug("Created user event: {}", event);

        // When
        log.info("Sending event to Kafka topic 'user-events'");
        kafkaTemplate.send("user-events", event);
        log.debug("Event sent successfully");

        // Ждем обработки события
        log.info("Waiting for event processing...");
        Thread.sleep(5000);
        log.info("Event processing completed");

        // Then - проверяем что email был бы отправлен
        verify(mailSender).send(any(SimpleMailMessage.class));
        assertTrue(true);
    }

    @Test
    void shouldSendEmailOnUserDeletedEvent() throws Exception {
        // Given
        UserEvent event = new UserEvent("USER_DELETED", "test@example.com", 1L);

        // When
        kafkaTemplate.send("user-events", event);

        // Then - ждем и проверяем
        Thread.sleep(5000);
        verify(mailSender).send(any(SimpleMailMessage.class));
        assertTrue(true);
    }
}