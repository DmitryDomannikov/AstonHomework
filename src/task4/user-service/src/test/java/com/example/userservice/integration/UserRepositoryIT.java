package com.example.userservice.integration;

import com.example.userservice.UserServiceApplication;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {UserServiceApplication.class, UserMapper.class})
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserRepositoryIT {

    private static final String NAME = "John";
    private static final String EMAIL = "john@test.com";
    private static final int AGE = 30;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void saveAndRetrieveUser_ShouldWork() {
        User user = User.builder()
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .build();

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(EMAIL, foundUser.get().getEmail());
        assertEquals(NAME, foundUser.get().getName());
        assertEquals(AGE, foundUser.get().getAge());
    }

    @Test
    void existsByEmail_ShouldReturnCorrectResult() {
        User user = User.builder()
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(EMAIL));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    void findByAgeBetween_ShouldReturnCorrectUsers() {
        User user1 = User.builder().name("Alice").email("alice@test.com").age(25).build();
        User user2 = User.builder().name("Bob").email("bob@test.com").age(35).build();
        User user3 = User.builder().name("Charlie").email("charlie@test.com").age(45).build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findByAgeBetween(20, 40, pageable);

        assertEquals(2, result.getTotalElements()); // Проверяем общее количество подходящих пользователей
        assertTrue(result.getContent().stream().anyMatch(u -> u.getName().equals("Alice")));
        assertTrue(result.getContent().stream().anyMatch(u -> u.getName().equals("Bob")));
    }
}
