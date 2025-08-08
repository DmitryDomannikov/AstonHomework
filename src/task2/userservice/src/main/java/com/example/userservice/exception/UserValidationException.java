package com.example.userservice.exception;

public class UserValidationException extends RuntimeException{
    public UserValidationException(String message) {
        super(message);
    }
}
