package com.bookstore.security.service;

import com.bookstore.security.dto.LoginRequest;
import com.bookstore.security.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}