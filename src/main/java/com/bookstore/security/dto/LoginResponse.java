package com.bookstore.security.dto;

import com.bookstore.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO returned after successful authentication.
 *
 * <p>
 * It contains authentication tokens together with non-sensitive
 * information about the authenticated user.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Authentication token information.
     */
    private TokenResponse tokens;

    /**
     * Authenticated user's public profile information.
     */
    private UserResponse user;
}