package com.example.userservice.util;

import com.example.userservice.exception.UserValidationException;
import com.example.userservice.model.User;
import jakarta.validation.*;

import java.util.Set;
import java.util.stream.Collectors;

public class UserValidator {
    public static void validateUser(User user) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new UserValidationException(errorMessage);
        }
    }
}