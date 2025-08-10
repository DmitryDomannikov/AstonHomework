package com.example.userservice.dao;

import com.example.userservice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    void save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void update(User user);
    void delete(User user);
    boolean existsByEmail(String email);
}

