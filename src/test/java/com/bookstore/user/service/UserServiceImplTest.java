package com.bookstore.user.service;

import com.bookstore.common.enums.UserStatus;
import com.bookstore.user.dto.UserRegistrationRequest;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.dto.UserUpdateRequest;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = User.builder()
                .firstName("Ishaan")
                .lastName("Singh")
                .email("ishaan@gmail.com")
                .password("encodedPassword")
                .phoneNumber("9876543210")
                .build();

        user.setId(userId);
    }

    @Test
    void register_shouldCreateUserSuccessfully() {

        UserRegistrationRequest request =
                UserRegistrationRequest.builder()
                        .firstName("Ishaan")
                        .lastName("Singh")
                        .email("ishaan@gmail.com")
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        when(userRepository.existsByEmailIgnoreCase("ishaan@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response =
                userService.register(request);

        assertNotNull(response);
        assertEquals("Ishaan", response.getFirstName());
        assertEquals("Singh", response.getLastName());
        assertEquals("ishaan@gmail.com", response.getEmail());
        assertEquals("9876543210", response.getPhoneNumber());

        verify(userRepository)
                .existsByEmailIgnoreCase("ishaan@gmail.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_shouldRejectDuplicateEmail() {

        UserRegistrationRequest request =
                UserRegistrationRequest.builder()
                        .firstName("Ishaan")
                        .lastName("Singh")
                        .email("ishaan@gmail.com")
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        when(userRepository.existsByEmailIgnoreCase("ishaan@gmail.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        verify(userRepository)
                .existsByEmailIgnoreCase("ishaan@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(any(String.class));
    }

    @Test
    void getUserById_shouldReturnUser() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Ishaan", response.getFirstName());
        assertEquals("Singh", response.getLastName());
        assertEquals("ishaan@gmail.com", response.getEmail());

        verify(userRepository)
                .findById(userId);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(userId)
        );

        verify(userRepository)
                .findById(userId);
    }

    @Test
    void updateProfile_shouldUpdateUser() {

        UserUpdateRequest request =
                UserUpdateRequest.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phoneNumber("9999999999")
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response =
                userService.updateProfile(userId, request);

        assertNotNull(response);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("9999999999", user.getPhoneNumber());

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateStatus_shouldUpdateUserStatus() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response =
                userService.updateStatus(
                        userId,
                        UserStatus.BLOCKED
                );

        assertNotNull(response);

        assertEquals(
                UserStatus.BLOCKED,
                user.getStatus()
        );

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .save(user);
    }
}