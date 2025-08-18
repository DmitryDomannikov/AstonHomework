package com.example.userservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Пользователь с email " + email + " уже существует");
    }
}
