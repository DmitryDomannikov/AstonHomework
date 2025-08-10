package com.example.userservice.util;

import com.example.userservice.exception.UserValidationException;
import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    @Test
    void validUser_NoException() {
        User user = User.builder().name("Name").email("a@b.com").age(30).build();
        assertDoesNotThrow(() -> UserValidator.validateUser(user));
    }

    @Test
    void blankName_Throws() {
        User user = User.builder().name("").email("a@b.com").age(30).build();
        assertThrows(UserValidationException.class, () -> UserValidator.validateUser(user));
    }

    @Test
    void invalidEmail_Throws() {
        User user = User.builder().name("Name").email("invalid").age(30).build();
        assertThrows(UserValidationException.class, () -> UserValidator.validateUser(user));
    }

    @Test
    void negativeAge_Throws() {
        User user = User.builder().name("Name").email("a@b.com").age(-1).build();
        assertThrows(UserValidationException.class, () -> UserValidator.validateUser(user));
    }

    @Test
    void tooOld_Throws() {
        User user = User.builder().name("Name").email("a@b.com").age(101).build();
        assertThrows(UserValidationException.class, () -> UserValidator.validateUser(user));
    }
}