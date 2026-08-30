package com.bookstore.user.controller;

import com.bookstore.common.enums.UserStatus;
import com.bookstore.security.JwtService;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /**
     * Mock required by JwtAuthenticationFilter
     * when the MVC test context loads the security configuration.
     */
    @MockitoBean
    private JwtService jwtService;

    /**
     * Mock required by JwtAuthenticationFilter and
     * Spring Security authentication configuration.
     */
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "ishaan@gmail.com", roles = "CUSTOMER")
    void register_shouldReturn201() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Ishaan")
                .lastName("Singh")
                .email("ishaan@gmail.com")
                .build();

        when(userService.register(any()))
                .thenReturn(response);

        String request = """
                {
                    "firstName": "Ishaan",
                    "lastName": "Singh",
                    "email": "ishaan@gmail.com",
                    "password": "password123",
                    "phoneNumber": "9876543210"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "ishaan@gmail.com", roles = "CUSTOMER")
    void getUser_shouldReturn200() throws Exception {

        UUID id = UUID.randomUUID();

        UserResponse response = UserResponse.builder()
                .id(id)
                .firstName("Ishaan")
                .lastName("Singh")
                .email("ishaan@gmail.com")
                .build();

        when(userService.getUserById(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/{id}", id)
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ishaan@gmail.com", roles = "CUSTOMER")
    void updateProfile_shouldReturn200() throws Exception {

        UUID id = UUID.randomUUID();

        UserResponse response = UserResponse.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@gmail.com")
                .build();

        when(userService.updateProfile(eq(id), any()))
                .thenReturn(response);

        String request = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "phoneNumber": "9999999999"
                }
                """;

        mockMvc.perform(
                        put("/api/users/{id}", id)
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ishaan@gmail.com", roles = "CUSTOMER")
    void updateStatus_shouldReturn200() throws Exception {

        UUID id = UUID.randomUUID();

        UserResponse response = UserResponse.builder()
                .id(id)
                .status(UserStatus.BLOCKED)
                .build();

        when(userService.updateStatus(id, UserStatus.BLOCKED))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/users/{id}/status", id)
                                .with(csrf())
                                .param("status", "BLOCKED")
                )
                .andExpect(status().isOk());
    }
}