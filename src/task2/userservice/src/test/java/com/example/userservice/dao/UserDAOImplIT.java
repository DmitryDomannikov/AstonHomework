package com.example.userservice.dao;

import com.example.userservice.model.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDAOImplIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());

        userDAO = new UserDAOImpl();
        // Проверяем, что можем установить соединение
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Should be connected to database");
        }
    }

    @BeforeEach
    void clearDatabase() {
        HibernateUtil.getSessionFactory().inTransaction(session -> {
            session.createMutationQuery("DELETE FROM User").executeUpdate();
        });
    }

    @AfterAll
    static void cleanup() {
        HibernateUtil.shutdown();
    }

    @Test
    void testSaveAndFindUser() {
        // Создаем пользователя
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setAge(25);

        // Сохраняем
        userDAO.save(user);

        // Ищем по ID
        Optional<User> foundUser = userDAO.findById(user.getId());

        // Проверяем
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john@example.com", foundUser.get().getEmail());
        assertEquals(25, foundUser.get().getAge());
    }

    @Test
    void testFindAllUsers() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setName("Alice");
        user1.setEmail("alice@example.com");
        user1.setAge(30);

        User user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@example.com");
        user2.setAge(35);

        // Сохраняем
        userDAO.save(user1);
        userDAO.save(user2);

        // Получаем всех
        List<User> users = userDAO.findAll();

        // Проверяем
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() {
        // Создаем и сохраняем пользователя
        User user = new User();
        user.setName("Original Name");
        user.setEmail("original@example.com");
        user.setAge(40);
        userDAO.save(user);

        // Обновляем
        user.setName("Updated Name");
        userDAO.update(user);

        // Проверяем
        Optional<User> updatedUser = userDAO.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Updated Name", updatedUser.get().getName());
    }

    @Test
    void testDeleteUser() {
        // Создаем и сохраняем пользователя
        User user = new User();
        user.setName("To Delete");
        user.setEmail("delete@example.com");
        user.setAge(45);
        userDAO.save(user);

        // Удаляем
        userDAO.delete(user);

        // Проверяем
        Optional<User> deletedUser = userDAO.findById(user.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindNonExistentUser() {
        Optional<User> user = userDAO.findById(999L);
        assertFalse(user.isPresent());
    }

    @Test
    void testEmailExists() {
        // Создаем пользователя
        User user = new User();
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setAge(20);
        userDAO.save(user);

        // Проверяем
        assertTrue(userDAO.existsByEmail("test@example.com"));
        assertFalse(userDAO.existsByEmail("nonexistent@example.com"));
    }
}