package com.example.userservice.dao;

import com.example.userservice.model.User;
import com.example.userservice.util.HibernateUtil;
import com.example.userservice.util.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserDAOImpl implements UserDAO {

    @Override
    public void save(User user) {
        // Автоматическая валидация перед сохранением
        try {
            UserValidator.validateUser(user);
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации пользователя: {}", e.getMessage());
            throw e;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            log.info("Пользователь сохранен: ID={}, email={}", user.getId(), user.getEmail());
        } catch (Exception e) {
            handleTransactionRollback(tx);
            log.error("Ошибка при сохранении пользователя: {}", user, e);
            throw new RuntimeException("Ошибка при сохранении пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            log.warn("Попытка поиска пользователя с null-ID");
            return Optional.empty();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user == null) {
                log.warn("Пользователь с ID={} не найден", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователя по ID={}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.getResultList();
            log.debug("Найдено пользователей: {}", users.size());
            return users;
        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей", e);
            return List.of();
        }
    }

    @Override
    public void update(User user) {
        validateUserWithId(user, "update");
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            log.info("Пользователь обновлен: ID={}", user.getId());
        } catch (Exception e) {
            handleTransactionRollback(tx);
            log.error("Ошибка при обновлении пользователя: ID={}", user.getId(), e);
            throw new RuntimeException("Ошибка при обновлении пользователя", e);
        }
    }

    @Override
    public void delete(User user) {
        validateUserWithId(user, "delete");
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.contains(user) ? user : session.merge(user));
            tx.commit();
            log.info("Пользователь удален: ID={}", user.getId());
        } catch (Exception e) {
            handleTransactionRollback(tx);
            log.error("Ошибка при удалении пользователя: ID={}", user.getId(), e);
            throw new RuntimeException("Ошибка при удалении пользователя", e);
        }
    }

    private void validateUser(User user, String operation) {
        if (user == null) {
            log.error("Попытка {} null-пользователя", operation);
            throw new IllegalArgumentException("User cannot be null for operation: " + operation);
        }
    }

    private void validateUserWithId(User user, String operation) {
        validateUser(user, operation);
        if (user.getId() == null) {
            log.error("Попытка {} пользователя без ID", operation);
            throw new IllegalArgumentException("User ID cannot be null for operation: " + operation);
        }
    }

    private void handleTransactionRollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
        }
    }
}