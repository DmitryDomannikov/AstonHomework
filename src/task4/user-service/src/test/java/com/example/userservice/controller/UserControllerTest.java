package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.GlobalExceptionHandler;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.service.HateoasService;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final Long USER_ID = 1L;
    private static final String NAME = "John";
    private static final String EMAIL = "john@test.com";
    private static final int AGE = 30;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private HateoasService hateoasService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserRequest buildUserRequest(String name, String email, int age) {
        return UserRequest.builder()
                .name(name)
                .email(email)
                .age(age)
                .build();
    }

    private UserResponse buildUserResponse(Long id, String name, String email, int age) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .age(age)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        UserRequest request = buildUserRequest(NAME, EMAIL, AGE);
        UserResponse response = buildUserResponse(USER_ID, NAME, EMAIL, AGE);

        given(userService.createUser(any(UserRequest.class))).willReturn(response);
        given(hateoasService.addLinksToUser(any(UserResponse.class))).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.age").value(AGE));
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserRequest request = buildUserRequest("Updated", "updated@test.com", 25);
        UserResponse response = buildUserResponse(USER_ID, "Updated", "updated@test.com", 25);

        given(userService.updateUser(eq(USER_ID), any(UserRequest.class))).willReturn(response);
        given(hateoasService.addLinksToUser(any(UserResponse.class))).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put(BASE_URL + "/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        UserRequest request = buildUserRequest("Updated", "updated@test.com", 25);

        given(userService.updateUser(eq(999L), any(UserRequest.class)))
                .willThrow(new UserNotFoundException(999L));

        mockMvc.perform(put(BASE_URL + "/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        UserResponse response = buildUserResponse(USER_ID, NAME, EMAIL, AGE);

        given(userService.getUserById(USER_ID)).willReturn(response);
        given(hateoasService.addLinksToUser(any(UserResponse.class))).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get(BASE_URL + "/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturnConflict() throws Exception {
        UserRequest request = buildUserRequest(NAME, EMAIL, AGE);

        given(userService.createUser(any(UserRequest.class)))
                .willThrow(new UserAlreadyExistsException(EMAIL));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
