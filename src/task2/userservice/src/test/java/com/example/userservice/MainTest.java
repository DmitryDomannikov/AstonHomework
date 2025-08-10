package com.example.userservice;

import com.example.userservice.dao.UserDAO;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.exception.UserValidationException;
import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private Scanner scanner;

    @InjectMocks
    private Main main;

    // Тест на успешное создание
    @Test
    void createUser_Success() {
        when(scanner.nextLine())
                .thenReturn("Alex")
                .thenReturn("alex@mail.com")
                .thenReturn("25");

        main.createUser();
        verify(userDAO).save(any(User.class)); // Проверка что save() был вызван
    }

    @Test
    void createUser_ValidationFailed() {
        when(scanner.nextLine())
                .thenReturn("")
                .thenReturn("invalid-email")
                .thenReturn("200");

        assertThrows(UserValidationException.class, () -> main.createUser());
    }

    @Test
    void createUser_EmailExists() {   // Тест на дубликат email
        when(scanner.nextLine())
                .thenReturn("Alex")
                .thenReturn("exists@mail.com")
                .thenReturn("25");

        when(userDAO.existsByEmail("exists@mail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> main.createUser());
    }

    @Test
    void listUsers_EmptyList() {    // Тест пустого списка пользователей
        when(userDAO.findAll()).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> main.listUsers());
    }

    @Test
    void findUser_Success() {       // Тест поиска существующего пользователя
        when(scanner.nextLine()).thenReturn("1");
        when(userDAO.findById(1L)).thenReturn(Optional.of(new User()));
        assertDoesNotThrow(() -> main.findUser());
    }

    @Test
    void updateUser_Success() {      // Тест обновления пользователя
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("New Name")
                .thenReturn("")
                .thenReturn("30");

        when(userDAO.findById(1L)).thenReturn(Optional.of(new User()));
        main.updateUser();
        verify(userDAO).update(any(User.class));
    }


    @Test
    void deleteUser_Success() {    // Тест удаления пользователя
        when(scanner.nextLine()).thenReturn("1");
        when(userDAO.findById(1L)).thenReturn(Optional.of(new User()));
        main.deleteUser();
        verify(userDAO).delete(any(User.class));
    }
}