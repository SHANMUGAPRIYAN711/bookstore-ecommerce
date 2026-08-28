package com.bookstore.user.dto;

import com.bookstore.common.enums.AuthProvider;
import com.bookstore.common.enums.Role;
import com.bookstore.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing non-sensitive user information returned
 * by the REST API.
 *
 * <p>
 * Passwords and other authentication secrets are intentionally excluded.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * Unique identifier of the user.
     */
    private UUID id;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's email address.
     */
    private String email;

    /**
     * User's phone number.
     */
    private String phoneNumber;

    /**
     * Application role assigned to the user.
     */
    private Role role;

    /**
     * Current account status.
     */
    private UserStatus status;

    /**
     * Authentication provider associated with the account.
     */
    private AuthProvider authProvider;

    /**
     * Timestamp at which the account was created.
     */
    private Instant createdAt;

    /**
     * Timestamp at which the account was last updated.
     */
    private Instant updatedAt;
}