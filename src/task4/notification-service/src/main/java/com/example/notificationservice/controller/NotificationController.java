package com.example.notificationservice.controller;

import com.example.notificationservice.dto.EmailRequest;
import com.example.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "Управление уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(summary = "Отправить email вручную", description = "Отправка произвольного email сообщения")
    @ApiResponse(responseCode = "200", description = "Email успешно отправлен")
    @ApiResponse(responseCode = "400", description = "Ошибка отправки email")
    @PostMapping("/email")
    public ResponseEntity<String> sendManualEmail(@RequestBody EmailRequest request) {
        try {
            notificationService.sendEmail(request.email(), request.subject(), request.message());
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }
}