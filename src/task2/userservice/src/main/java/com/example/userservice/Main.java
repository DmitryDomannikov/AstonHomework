package com.example.userservice;

import com.example.userservice.dao.UserDAO;
import com.example.userservice.dao.UserDAOImpl;
import com.example.userservice.model.User;
import com.example.userservice.util.UserValidator;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@RequiredArgsConstructor
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAOImpl();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createUser();
                case "2" -> listUsers();
                case "3" -> findUser();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "0" -> running = false;
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }

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

            // Создаем временный объект для валидации
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(age)
                    .build();

            UserValidator.validateUser(user);
            userDAO.save(user);
            System.out.println("✅ Пользователь создан: " + user);

        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: Возраст должен быть числом");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Неожиданная ошибка: " + e.getMessage());
        }
    }

    private static void listUsers() {
        List<User> users = userDAO.findAll();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void findUser() {
        System.out.print("Введите ID пользователя: ");
        Long id = Long.parseLong(scanner.nextLine());

        Optional<User> user = userDAO.findById(id);
        if (user.isPresent()) {
            System.out.println("Найден пользователь: " + user);
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> userOpt = userDAO.findById(id);
            if (userOpt.isEmpty()) {
                System.out.println("❌ Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            System.out.print("Новое имя (" + user.getName() + "): ");
            String name = scanner.nextLine();
            if (!name.isBlank()) {
                user.setName(name.trim());
            }

            System.out.print("Новый email (" + user.getEmail() + "): ");
            String email = scanner.nextLine();
            if (!email.isBlank()) {
                user.setEmail(email.trim());
            }

            System.out.print("Новый возраст (" + user.getAge() + "): ");
            String ageStr = scanner.nextLine();
            if (!ageStr.isBlank()) {
                user.setAge(Integer.parseInt(ageStr));
            }

            UserValidator.validateUser(user);
            userDAO.update(user);
            System.out.println("✅ Пользователь обновлён: " + user);

        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: Неккоректные данные");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Неожиданная ошибка: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = Long.parseLong(scanner.nextLine());

        Optional<User> user = userDAO.findById(id);
        if (user.isEmpty()) {
            System.out.println("Пользователь не найден.");
            return;
        }

        userDAO.delete(user.get());
        System.out.println("🗑 Пользователь удалён: " + user.get().getName());
    }
}
