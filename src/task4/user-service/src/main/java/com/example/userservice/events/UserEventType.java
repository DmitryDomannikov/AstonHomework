package com.example.userservice.events;

public enum UserEventType {
    USER_CREATED("user.created"),
    USER_DELETED("user.deleted"),
    USER_UPDATED("user.updated"); // На будущее

    private final String eventType;

    UserEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }
}