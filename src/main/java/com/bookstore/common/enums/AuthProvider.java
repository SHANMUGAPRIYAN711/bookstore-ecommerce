package com.bookstore.common.enums;

/**
 * Defines the authentication providers supported by the application.
 */
public enum AuthProvider {

    /**
     * Traditional application-managed email/password authentication.
     */
    LOCAL,

    /**
     * Google OAuth2/SSO authentication.
     */
    GOOGLE
}