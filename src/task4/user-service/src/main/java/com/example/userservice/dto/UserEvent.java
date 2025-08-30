package com.example.userservice.dto;

public record UserEvent( String eventType, String email, Long userId) {}