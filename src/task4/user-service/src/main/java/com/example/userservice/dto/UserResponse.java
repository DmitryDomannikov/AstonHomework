package com.example.userservice.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserResponse extends RepresentationModel<UserResponse> {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getStatus() {
        if (age == null) return "UNKNOWN";
        return age >= 18 ? "ADULT" : "MINOR";
    }
}