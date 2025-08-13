package com.example.userservice.dao;

import com.example.userservice.model.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
    // Константы для контейнера
    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DATABASE_NAME = "testdb";
    private static final String DATABASE_USERNAME = "testuser";
    private static final String DATABASE_PASSWORD = "testpass";

    // Константы для тестовых данных
    private static final String USER1_NAME = "John Johnson";
    private static final String USER1_EMAIL = "john@example.com";
    private static final int USER1_AGE = 25;

    private static final String USER2_NAME = "Alice";
    private static final String USER2_EMAIL = "alice@example.com";
    private static final int USER2_AGE = 30;

    private static final String UPDATED_NAME = "Updated Name";

    private static final long NON_EXISTENT_ID = 999L;

        @Container
        private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
                new PostgreSQLContainer<>(POSTGRES_IMAGE)
                        .withDatabaseName(DATABASE_NAME )
                        .withUsername(DATABASE_USERNAME)
                        .withPassword(DATABASE_PASSWORD);

        private static SessionFactory sessionFactory;
        private UserDAO userDAO;

        @BeforeAll
        static void setup() {
            assertTrue(POSTGRES_CONTAINER.isRunning(), "PostgreSQL container should be running");
            System.setProperty("hibernate.connection.url", POSTGRES_CONTAINER.getJdbcUrl());
            System.setProperty("hibernate.connection.username", POSTGRES_CONTAINER.getUsername());
            System.setProperty("hibernate.connection.password", POSTGRES_CONTAINER.getPassword());

            sessionFactory = HibernateUtil.getSessionFactory();

            try (Session session = sessionFactory.openSession()) {
                assertTrue(session.isConnected(), "Should be connected to database");
            }
        }

        @BeforeEach
        void setUp() {
            userDAO = new UserDAOImpl();
            clearDatabase();
        }

        @AfterAll
        static void cleanup() {
            HibernateUtil.shutdown();
        }

        private void clearDatabase() {
            sessionFactory.inTransaction(session -> {
                session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
            });
        }

        private User persistTestUser(String name, String email, int age) {
            return sessionFactory.fromTransaction(session -> {
                User user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setAge(age);
                session.persist(user);
                return user;
            });
        }

        @Test
        void findById_shouldReturnUser_whenUserExists() {
            // Подготовка данных
            User expectedUser = persistTestUser(USER1_NAME, USER1_EMAIL, USER1_AGE);

            Optional<User> found = userDAO.findById(expectedUser.getId());

            // Проверки
            assertTrue(found.isPresent());
            assertEquals(expectedUser.getName(), found.get().getName());
            assertEquals(expectedUser.getEmail(), found.get().getEmail());
            assertEquals(expectedUser.getAge(), found.get().getAge());
        }

        @Test
        void update_shouldModifyExistingUser() {
            // Подготовка данных
            User originalUser = persistTestUser(USER1_NAME, USER1_EMAIL, USER1_AGE);

            originalUser.setName(UPDATED_NAME);

            userDAO.update(originalUser);

            User updatedUser = sessionFactory.fromTransaction(session ->
                    session.find(User.class, originalUser.getId())
            );

            assertEquals(UPDATED_NAME, updatedUser.getName());
            assertEquals(USER1_EMAIL, updatedUser.getEmail());
            assertEquals(USER1_AGE, updatedUser.getAge());
        }

        @Test
        void save_shouldCreateNewUser() {
            // Создаем новый объект
            User newUser = new User();
            newUser.setName(USER1_NAME);
            newUser.setEmail(USER1_EMAIL);
            newUser.setAge(USER1_AGE);

            userDAO.save(newUser);

            User savedUser = sessionFactory.fromTransaction(session ->
                    session.find(User.class, newUser.getId())
            );

            assertNotNull(savedUser);
            assertEquals(USER1_NAME, savedUser.getName());
            assertEquals(USER1_EMAIL, savedUser.getEmail());
            assertEquals(USER1_AGE, savedUser.getAge());
        }

        @Test
        void findAll_shouldReturnAllUsers() {
            // Подготовка данных
            User user1 = persistTestUser(USER1_NAME, USER1_EMAIL, USER1_AGE);
            User user2 = persistTestUser(USER2_NAME, USER2_EMAIL, USER2_AGE);

            List<User> users = userDAO.findAll();

            // Проверка
            assertEquals(2, users.size());
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(user1.getId())));
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        }

        @Test
        void delete_shouldRemoveUser() {
            User userToDelete = persistTestUser(USER1_NAME, USER1_EMAIL, USER1_AGE);

            userDAO.delete(userToDelete);

            // Проверяем чтение из БД
            Optional<User> deletedUser = sessionFactory.fromTransaction(session ->
                    Optional.ofNullable(session.find(User.class, userToDelete.getId()))
            );

            assertFalse(deletedUser.isPresent());
        }

        @Test
        void existsByEmail_shouldReturnCorrectResult() {
            // Подготовка данных
            persistTestUser(USER1_NAME, USER1_EMAIL, USER1_AGE);

            assertTrue(userDAO.existsByEmail(USER1_EMAIL));
            assertFalse(userDAO.existsByEmail("nonexistent@email.com"));
        }

        @Test
        void findById_shouldReturnEmptyOptional_whenUserNotExists() {
            Optional<User> result = userDAO.findById(NON_EXISTENT_ID);

            // Проверка
            assertFalse(result.isPresent());
        }
    }