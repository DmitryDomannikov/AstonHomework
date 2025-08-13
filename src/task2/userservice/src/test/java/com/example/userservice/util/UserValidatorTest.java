package com.example.userservice.util;

import com.example.userservice.exception.UserValidationException;
import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {
    private static final String VALID_NAME = "Name";
    private static final String VALID_EMAIL = "a@b.com";
    private static final int VALID_AGE = 30;
    private static final String BLANK_NAME = "";
    private static final String INVALID_EMAIL = "invalid";
    private static final int NEGATIVE_AGE = -1;
    private static final int TOO_OLD_AGE = 101;
    private static final int MAX_ALLOWED_AGE = 100;
    private static final int MIN_ALLOWED_AGE = 0;

    @Test
    void validUser_NoException() {
        User user = User.builder()
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .age(VALID_AGE)
                .build();

        assertDoesNotThrow(() -> UserValidator.validateUser(user));
    }

    @Test
    void blankName_Throws() {
        User user = User.builder()
                .name(BLANK_NAME)
                .email(VALID_EMAIL)
                .age(VALID_AGE)
                .build();

        assertThrows(UserValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void invalidEmail_Throws() {
        User user = User.builder()
                .name(VALID_NAME)
                .email(INVALID_EMAIL)
                .age(VALID_AGE)
                .build();

        assertThrows(UserValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void negativeAge_Throws() {
        User user = User.builder()
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .age(NEGATIVE_AGE)
                .build();

        assertThrows(UserValidationException.class,
                () -> UserValidator.validateUser(user),
                "Age should not be less than " + MIN_ALLOWED_AGE);
    }

    @Test
    void tooOld_Throws() {
        User user = User.builder()
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .age(TOO_OLD_AGE)
                .build();

        assertThrows(UserValidationException.class,
                () -> UserValidator.validateUser(user),
                "Age should not be more than " + MAX_ALLOWED_AGE);
    }
}