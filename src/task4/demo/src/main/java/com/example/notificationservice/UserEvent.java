package com.example.notificationservice;

public record UserEvent(String eventType, String email, Long userId) {}
