package com.bookstore.user.service;

import com.bookstore.common.enums.UserStatus;
import com.bookstore.user.dto.UserRegistrationRequest;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.dto.UserUpdateRequest;

import java.util.UUID;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    UserResponse getUserById(UUID id);

    UserResponse updateProfile(UUID id, UserUpdateRequest request);

    UserResponse updateStatus(UUID id, UserStatus status);
}