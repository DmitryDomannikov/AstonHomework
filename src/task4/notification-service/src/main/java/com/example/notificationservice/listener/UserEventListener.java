package com.example.notificationservice.listener;

import com.example.notificationservice.dto.UserEvent;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-events")
    public void handleUserEvent(UserEvent event) {
        notificationService.handleUserEvent(event);
    }
}
