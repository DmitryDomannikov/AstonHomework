package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.GlobalExceptionHandler;
import com.example.userservice.exception.UserAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String NAME = "John";
    private static final String EMAIL = "john@test.com";
    private static final int AGE = 30;
    private static final Long ID = 1L;
    private static final String URL = "/api/v1/users";

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    //Тест успешного создания пользователя
    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        UserRequest request = new UserRequest();
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setAge(AGE);

        UserResponse response = UserResponse.builder()
                .id(ID)
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .build();

        given(userService.createUser(any(UserRequest.class))).willReturn(response);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.age").value(AGE));
    }
     //Тест успешного обновления пользователя
    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Updated");
        request.setEmail("updated@test.com");
        request.setAge(25);

        UserResponse response = UserResponse.builder()
                .id(ID)
                .name("Updated")
                .email("updated@test.com")
                .age(25)
                .build();

        given(userService.updateUser(eq(ID), any(UserRequest.class)))
                .willReturn(response);

        mockMvc.perform(put(URL + "/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    //Тест попытки обновления несуществующего пользователя
    @Test
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Updated");
        request.setEmail("updated@test.com");
        request.setAge(25);

        given(userService.updateUser(eq(999L), any(UserRequest.class)))
                .willThrow(new UserNotFoundException(999L));

        mockMvc.perform(put(URL + "/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    // Добавить тесты для:
    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        UserResponse response = UserResponse.builder().id(ID).name(NAME).email(EMAIL).age(AGE).build();
        given(userService.getUserById(ID)).willReturn(response);

        mockMvc.perform(get(URL + "/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(ID);

        mockMvc.perform(delete(URL + "/{id}", ID))
                .andExpect(status().isNoContent());
    }

    //Тест создания пользователя с дублирующимся email
    @Test
    void createUser_DuplicateEmail_ShouldThrowConflict() throws Exception {
        // Подготовка тестовых данных
        UserRequest request = new UserRequest();
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setAge(AGE);

        given(userService.createUser(any())).willThrow(new UserAlreadyExistsException(EMAIL));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}