package com.example.customnotification.integration;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(KafkaConfigurationTest.TestConfig.class)
class KafkaConfigurationTest {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfigurationTest.class);

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
    private KafkaTemplate<String, Object> kafkaTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafka.getBootstrapServers();
        log.info("Configuring Kafka bootstrap servers: {}", bootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);

        // Добавляем настройки сериализации
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers",
                () -> "false");
    }

    @Test
    void kafkaTemplateShouldBeConfigured() {
        log.debug("Testing Kafka template configuration");
        assertNotNull(kafkaTemplate);
        log.info("Kafka template is properly configured");
    }
}