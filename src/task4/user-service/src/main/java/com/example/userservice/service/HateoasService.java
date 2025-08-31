package com.example.userservice.service;

import com.example.userservice.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import com.example.userservice.controller.UserController;

@Service
public class HateoasService {

    public Page<UserResponse> addLinksToPage(Page<UserResponse> users) {
        users.getContent().forEach(this::addLinksToUser);
        return users;
    }

    public UserResponse addLinksToUser(UserResponse user) {
        user.add(linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel());
        user.add(linkTo(methodOn(UserController.class).getAllUsers(null)).withRel("all-users"));
        user.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
        return user;
    }
}
