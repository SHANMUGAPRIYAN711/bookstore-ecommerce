package com.bookstore.security.service;

import com.bookstore.security.CustomUserDetailsService;
import com.bookstore.security.JwtService;
import com.bookstore.security.dto.LoginRequest;
import com.bookstore.security.dto.LoginResponse;
import com.bookstore.security.dto.TokenResponse;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        String email =
                authentication.getName();

        User user =
                userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        String accessToken =
                jwtService.generateToken(
                        user.getEmail()
                );

        TokenResponse tokenResponse =
                TokenResponse.builder()
                        .accessToken(accessToken)
                        .tokenType("Bearer")
                        .expiresIn(86400)
                        .refreshToken(null)
                        .build();

        UserResponse userResponse =
                UserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .authProvider(user.getAuthProvider())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build();

        return LoginResponse.builder()
                .tokens(tokenResponse)
                .user(userResponse)
                .build();
    }
}