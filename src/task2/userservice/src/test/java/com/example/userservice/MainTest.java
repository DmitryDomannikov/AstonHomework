package com.example.userservice;

import com.example.userservice.dao.UserDAO;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.exception.UserValidationException;
import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private static final String USER_NAME = "Alex";
    private static final String USER_EMAIL = "alex@mail.com";
    private static final String USER_AGE = "25";
    private static final long USER_ID = 1L;
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String INVALID_AGE = "200";
    private static final String EXISTING_EMAIL = "exists@mail.com";
    private static final String NEW_NAME = "New Name";
    private static final String EMPTY_STRING = "";
    private static final String NEW_AGE = "30";
    private static final String DELETE_USER_NAME = "Delete Me";
    private static final String DELETE_USER_EMAIL = "delete@mail.com";
    private static final String DELETE_USER_AGE = "40";
    private static final String EXIT_COMMAND = "0";

    @Mock
    private UserDAO userDAO;

    @Mock
    private Scanner scanner;

    @InjectMocks
    private Main main;

    @Test
    void createUser_Success() {
        when(scanner.nextLine())
                .thenReturn(USER_NAME)
                .thenReturn(USER_EMAIL)
                .thenReturn(USER_AGE);

        main.createUser();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(USER_NAME, savedUser.getName());
        assertEquals(USER_EMAIL, savedUser.getEmail());
        assertEquals(Integer.parseInt(USER_AGE), savedUser.getAge());
    }

    @Test
    void createUser_ValidationFailed() {
        when(scanner.nextLine())
                .thenReturn(EMPTY_STRING)
                .thenReturn(INVALID_EMAIL)
                .thenReturn(INVALID_AGE);

        assertThrows(UserValidationException.class, () -> main.createUser());
        verify(userDAO, never()).save(any());
    }

    @Test
    void createUser_EmailExists() {
        when(scanner.nextLine())
                .thenReturn(USER_NAME)
                .thenReturn(EXISTING_EMAIL)
                .thenReturn(USER_AGE);

        when(userDAO.existsByEmail(EXISTING_EMAIL)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> main.createUser());
        verify(userDAO, never()).save(any());
    }

    @Test
    void listUsers_EmptyList() {
        when(userDAO.findAll()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> main.listUsers());
        verify(userDAO).findAll();
    }

    @Test
    void findUser_Success() {
        User testUser = User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .email(USER_EMAIL)
                .age(Integer.parseInt(USER_AGE))
                .build();

        when(scanner.nextLine()).thenReturn(String.valueOf(USER_ID));
        when(userDAO.findById(USER_ID)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> main.findUser());
        verify(userDAO).findById(USER_ID);
    }

    @Test
    void updateUser_Success() {
        User existingUser = User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .email(USER_EMAIL)
                .age(Integer.parseInt(USER_AGE))
                .build();

        when(scanner.nextLine())
                .thenReturn(String.valueOf(USER_ID))  // ID
                .thenReturn(NEW_NAME)               // Новое имя
                .thenReturn(EMPTY_STRING)           // Пустой email (не менять)
                .thenReturn(NEW_AGE);                // Новый возраст

        when(userDAO.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        main.updateUser();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).update(captor.capture());

        User updatedUser = captor.getValue();
        assertEquals(NEW_NAME, updatedUser.getName());
        assertEquals(USER_EMAIL, updatedUser.getEmail());
        assertEquals(Integer.parseInt(NEW_AGE), updatedUser.getAge());
    }

    @Test
    void deleteUser_Success() {
        User testUser = User.builder()
                .id(USER_ID)
                .name(DELETE_USER_NAME)
                .email(DELETE_USER_EMAIL)
                .age(Integer.parseInt(DELETE_USER_AGE))
                .build();

        when(scanner.nextLine()).thenReturn(String.valueOf(USER_ID));
        when(userDAO.findById(USER_ID)).thenReturn(Optional.of(testUser));

        main.deleteUser();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).delete(captor.capture());

        User deletedUser = captor.getValue();
        assertEquals(DELETE_USER_NAME, deletedUser.getName());
    }

    @Test
    void run_ExitCommand_ShouldTerminate() {
        when(scanner.nextLine())
                .thenReturn(EXIT_COMMAND); // Команда выхода

        main.run();

        // Проверяем что не было попыток работать с DAO
        verify(userDAO, never()).save(any());
        verify(userDAO, never()).update(any());
        verify(userDAO, never()).delete(any());
    }
}