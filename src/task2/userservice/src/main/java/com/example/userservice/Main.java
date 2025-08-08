package com.example.userservice;

import com.example.userservice.dao.UserDAO;
import com.example.userservice.dao.UserDAOImpl;
import com.example.userservice.exception.*;
import com.example.userservice.model.User;
import com.example.userservice.util.UserValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAOImpl();

    public static void main(String[] args) {
        log.info("Запуск приложения");
        boolean running = true;

        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            log.debug("Выбрано меню: {}", choice);

            switch (choice) {
                case "1" -> createUser();
                case "2" -> listUsers();
                case "3" -> findUser();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "0" -> running = false;
                default -> {
                    log.warn("Неверный выбор меню: {}", choice);
                    System.out.println("Неверный выбор. Попробуйте снова.");
                }
            }
        }

        log.info("Завершение работы приложения");
        System.out.println("Программа завершена.");
    }

    private static void showMenu() {
        System.out.println("""
                
                ========== Меню ==========
                1. Создать пользователя
                2. Показать всех пользователей
                3. Найти пользователя по ID
                4. Обновить пользователя
                5. Удалить пользователя
                0. Выход
                ==========================
                Выберите действие:
                """);
    }

    private static void createUser() {
        try {
            System.out.print("Имя: ");
            String name = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Возраст: ");
            int age = Integer.parseInt(scanner.nextLine());

            log.debug("Попытка создания пользователя: name={}, email={}, age={}", name, email, age);

            //временный объект для валидации
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(age)
                    .build();

            UserValidator.validateUser(user);
            userDAO.save(user);
            log.info("Создан пользователь: ID={}, email={}", user.getId(), user.getEmail());
            System.out.println("✅ Пользователь создан: " + user);

        } catch (NumberFormatException e) {
            log.warn("Некорректный возраст: {}", e.getMessage());
            System.out.println("❌ Ошибка: Возраст должен быть числом");
        } catch (UserValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            System.out.println("❌ Ошибка валидации: " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            log.warn("Попытка создать дубликат пользователя: {}", e.getMessage());
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (DatabaseOperationException e) {
            log.error("Ошибка БД при создании пользователя", e);
            System.out.println("❌ Ошибка базы данных: " + e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании пользователя", e);
            System.out.println("❌ Неожиданная ошибка: " + e.getMessage());
        }
    }

    private static void listUsers() {
        try {
            List<User> users = userDAO.findAll();
            if (users.isEmpty()) {
                log.info("Запрошен список пользователей - база пуста");
                System.out.println("ℹ️ Нет пользователей.");
            } else {
                log.info("Получено {} пользователей", users.size());
                System.out.println("Список пользователей:");
                users.forEach(System.out::println);
            }
        } catch (DatabaseOperationException e) {
            log.error("Ошибка при получении списка пользователей", e);
            System.out.println("❌ Ошибка при получении списка пользователей: " + e.getMessage());
        }
    }

    private static void findUser() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());
            log.debug("Поиск пользователя по ID: {}", id);

            Optional<User> user = userDAO.findById(id);
            if (user.isPresent()) {
                log.info("Найден пользователь: ID={}", id);
                System.out.println("Найден пользователь: " + user.get());
            } else {
                log.warn("Пользователь не найден: ID={}", id);
                System.out.println("Пользователь не найден.");
            }
        } catch (NumberFormatException e) {
            log.warn("Некорректный формат ID", e);
            System.out.println("❌ Ошибка: Некорректный ID");
        } catch (DatabaseOperationException e) {
            log.error("Ошибка при поиске пользователя", e);
            System.out.println("❌ Ошибка при поиске пользователя: " + e.getMessage());
        }
    }


    private static void updateUser() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());
            log.debug("Запрошено обновление пользователя: ID={}", id);

            User user = userDAO.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            System.out.print("Новое имя (" + user.getName() + "): ");
            String name = scanner.nextLine();
            if (!name.isBlank()) {
                user.setName(name.trim());
                log.debug("Обновлено имя пользователя ID={}", id);
            }

            System.out.print("Новый email (" + user.getEmail() + "): ");
            String email = scanner.nextLine();
            if (!email.isBlank()) {
                user.setEmail(email.trim());
                log.debug("Обновлен email пользователя ID={}", id);
            }

            System.out.print("Новый возраст (" + user.getAge() + "): ");
            String ageStr = scanner.nextLine();
            if (!ageStr.isBlank()) {
                user.setAge(Integer.parseInt(ageStr));
                log.debug("Обновлен возраст пользователя ID={}", id);
            }

            UserValidator.validateUser(user);
            userDAO.update(user);
            System.out.println("✅ Пользователь обновлён: " + user);
            log.info("Пользователь обновлен: ID={}", id);

        } catch (NumberFormatException e) {
            log.warn("Некорректные данные при обновлении", e);
            System.out.println("❌ Ошибка: Некорректные данные");
        } catch (UserValidationException e) {
            log.warn("Ошибка валидации при обновлении", e);
            System.out.println("❌ Ошибка валидации: " + e.getMessage());
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден при обновлении", e);
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (DatabaseOperationException e) {
            log.error("Ошибка БД при обновлении пользователя", e);
            System.out.println("❌ Ошибка базы данных: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Введите ID пользователя для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());
            log.debug("Запрошено удаление пользователя: ID={}", id);

            User user = userDAO.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            userDAO.delete(user);
            log.info("Пользователь удален: ID={}, name={}", id, user.getName());
            System.out.println("🗑 Пользователь удалён: " + user.getName());

        } catch (NumberFormatException e) {
            log.warn("Некорректный формат ID при удалении", e);
            System.out.println("❌ Ошибка: Некорректный ID");
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден при удалении", e);
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (DatabaseOperationException e) {
            log.error("Ошибка БД при удалении пользователя", e);
            System.out.println("❌ Ошибка при удалении пользователя: " + e.getMessage());
        }
    }
}
