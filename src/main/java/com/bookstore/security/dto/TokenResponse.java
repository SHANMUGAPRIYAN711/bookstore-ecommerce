package com.bookstore.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO containing JWT authentication tokens returned after successful
 * authentication.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * JWT access token used to authenticate API requests.
     */
    private String accessToken;

    /**
     * Token type used in the Authorization header.
     */
    private String tokenType;

    /**
     * Lifetime of the access token in seconds.
     */
    private long expiresIn;

    /**
     * Optional refresh token used to obtain a new access token.
     */
    private String refreshToken;
}