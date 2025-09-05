package com.example.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    // Константы для использования в сервисах
    public static final String USER_CREATED_TOPIC = "user-created";
    public static final String USER_DELETED_TOPIC = "user-deleted";
    public static final String USER_UPDATED_TOPIC = "user-updated";

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name(USER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name(USER_DELETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userUpdatedTopic() {
        return TopicBuilder.name(USER_UPDATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}