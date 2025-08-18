package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.*;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        log.info("Created user ID: {}", user.getId());
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        userMapper.updateFromRequest(request, user);
        user = userRepository.save(user);
        log.info("Updated user ID: {}", id);
        return userMapper.toResponse(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String name) {
        return userRepository.searchByName(name).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findUsersByAgeRange(int minAge, int maxAge, Pageable pageable) {
        return userRepository.findByAgeBetween(minAge, maxAge, pageable)
                .map(userMapper::toResponse);
    }
}