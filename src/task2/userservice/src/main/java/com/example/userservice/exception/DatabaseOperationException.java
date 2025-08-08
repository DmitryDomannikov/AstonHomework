package com.example.userservice.exception;

public class DatabaseOperationException extends  RuntimeException{
    public DatabaseOperationException(String operation, Throwable cause) {
        super("Ошибка при выполнении операции " + operation, cause);
    }
}
