package com.example.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Пользователь с ID " + id + " не найден");
    }

    public UserNotFoundException(String email) {
        super("Пользователь с email " + email + " не найден");
    }
}