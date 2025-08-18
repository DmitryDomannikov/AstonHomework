package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRequest {
    @NotBlank(message = "Имя обязательно")
    @Size(max = 50, message = "Максимальная длина имени - 50 символов")
    private String name;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotNull(message = "Возраст обязателен")
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 100, message = "Возраст не может быть больше 100")
    private Integer age;
}