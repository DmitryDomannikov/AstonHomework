package com.example.notificationservice.dto;

public record UserEvent(String eventType, String email, Long userId) {}
