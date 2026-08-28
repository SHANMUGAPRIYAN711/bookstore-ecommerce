package com.bookstore.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides utility methods for accessing information about
 * the currently authenticated user from Spring Security's
 * SecurityContext.
 */
public final class SecurityUtil {

    /**
     * Prevents instantiation of this utility class.
     */
    private SecurityUtil() {
    }

    /**
     * Returns the current Spring Security Authentication object.
     *
     * @return current authentication, or null when no authentication
     *         is available
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    /**
     * Returns the username or principal name of the currently
     * authenticated user.
     *
     * @return authenticated username, or null when no authenticated
     *         user is available
     */
    public static String getCurrentUsername() {

        Authentication authentication = getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }

    /**
     * Determines whether a user is currently authenticated.
     *
     * @return true when an authenticated user exists;
     *         otherwise false
     */
    public static boolean isAuthenticated() {

        Authentication authentication = getAuthentication();

        return authentication != null &&
                authentication.isAuthenticated();
    }
}