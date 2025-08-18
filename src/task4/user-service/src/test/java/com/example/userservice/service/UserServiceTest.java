package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {


    private static final String NAME = "John";
    private static final String EMAIL = "john@test.com";
    private static final int AGE = 30;
    private static final Long ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ValidRequest_ShouldReturnUserResponse() {
        UserRequest request = new UserRequest();
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setAge(AGE);

        User user = User.builder()
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .build();

        UserResponse response = new UserResponse();
        response.setId(ID);
        response.setName(NAME);
        response.setEmail(EMAIL);
        response.setAge(AGE);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertEquals(ID, result.getId());
        assertEquals(NAME, result.getName());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(AGE, result.getAge());

        //проверка вызовов
        verify(userRepository).existsByEmail(EMAIL);
        verify(userMapper).toEntity(request);
        verify(userMapper).toResponse(user);
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowException() {
        UserRequest request = new UserRequest();
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setAge(AGE);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });
    }
    @Test
    void updateUser_ShouldUpdateFields() {
        UserRequest request = new UserRequest(NAME, "new@email.com", 25);
        User user = User.builder().id(ID).name(NAME).email(EMAIL).age(AGE).build();
        User updatedUser = User.builder().id(ID).name(NAME).email("new@email.com").age(25).build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updatedUser);

        userService.updateUser(ID, request);
        verify(userMapper).updateFromRequest(request, user);
    }
}