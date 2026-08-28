package com.bookstore.common.constants;

/**
 * Contains shared constants used by the Spring Security
 * and JWT authentication components.
 */
public final class SecurityConstants {

    /**
     * Prevents instantiation of this utility class.
     */
    private SecurityConstants() {
    }

    /**
     * HTTP Authorization header name.
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Prefix used for Bearer authentication tokens.
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Authentication endpoint.
     */
    public static final String AUTH_ENDPOINT = "/api/auth/**";

    /**
     * OAuth2 authorization endpoint.
     */
    public static final String OAUTH2_AUTHORIZATION_ENDPOINT =
            "/oauth2/authorization/**";

    /**
     * OAuth2 callback endpoint.
     */
    public static final String OAUTH2_CALLBACK_ENDPOINT =
            "/login/oauth2/code/**";
}