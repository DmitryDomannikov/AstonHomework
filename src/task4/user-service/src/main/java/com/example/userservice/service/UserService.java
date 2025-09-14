package com.example.userservice.service;

import com.example.userservice.config.KafkaConfig;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.events.UserEventType;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        log.info("Created user ID: {}", user.getId());

        // Публикация события
        publishUserEventAsync(createUserEvent(user, UserEventType.USER_CREATED), KafkaConfig.USER_CREATED_TOPIC);

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

        boolean emailChanged = !user.getEmail().equals(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        userMapper.updateFromRequest(request, user);
        user = userRepository.save(user);
        log.info("Updated user ID: {}", id);

        publishUserEventAsync(createUserEvent(user, UserEventType.USER_UPDATED), KafkaConfig.USER_UPDATED_TOPIC);

        return userMapper.toResponse(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.deleteById(id);
        log.info("Deleted user ID: {}", id);

        // Публикация события удаления
        publishUserEventAsync(createUserEvent(user, UserEventType.USER_DELETED), KafkaConfig.USER_DELETED_TOPIC);
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

    // ----------------- Circuit Breaker + Retry + TimeLimiter для публикации событий -----------------

    @CircuitBreaker(name = "kafka-producer", fallbackMethod = "fallbackPublishEvent")
    @Retry(name = "kafka-producer")
    @TimeLimiter(name = "kafka-producer")
    public CompletableFuture<Void> publishUserEventAsync(Map<String, Object> payload, String topic) {
        return CompletableFuture.runAsync(() -> {
            kafkaTemplate.send(topic, payload);
            log.info("Published event to topic {}: {}", topic, payload);
        });
    }

    // Fallback
    @SuppressWarnings("unused")
    private CompletableFuture<Void> fallbackPublishEvent(Map<String, Object> payload, String topic, Throwable ex) {
        log.warn("Fallback publish to topic {} failed: {}. Payload={}", topic, ex.getMessage(), payload);
        // Здесь можно добавить Outbox/Retry очередь/Dead-letter логику
        return CompletableFuture.completedFuture(null);
    }

    private Map<String, Object> createUserEvent(User user, UserEventType eventType) {
        return Map.of(
                "eventType", eventType.getEventType(),
                "userId", user.getId(),
                "userEmail", user.getEmail(),
                "userName", user.getName(),
                "timestamp", Instant.now().toString()
        );
    }
}
