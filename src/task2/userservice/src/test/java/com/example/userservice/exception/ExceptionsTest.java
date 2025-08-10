package com.example.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void databaseOperationException_ShouldContainCorrectMessage() {
        Throwable cause = new RuntimeException("Test cause");
        DatabaseOperationException exception = new DatabaseOperationException("test operation", cause);

        assertEquals("Ошибка при выполнении операции test operation", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void userAlreadyExistsException_ShouldContainCorrectMessage() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertEquals("Пользователь с email test@example.comуже существует", exception.getMessage());
    }

    @Test
    void userNotFoundException_ShouldContainCorrectMessage() {
        UserNotFoundException exception = new UserNotFoundException(123L);

        assertEquals("Пользователь с ID 123не найден", exception.getMessage());
    }

    @Test
    void userValidationException_ShouldContainCorrectMessage() {
        UserValidationException exception = new UserValidationException("Test validation message");

        assertEquals("Test validation message", exception.getMessage());
    }
}
