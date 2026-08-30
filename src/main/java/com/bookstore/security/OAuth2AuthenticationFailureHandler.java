package com.bookstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles failed OAuth2 authentication attempts.
 *
 * <p>
 * This handler provides a controlled response when authentication
 * through an OAuth2 provider fails.
 * </p>
 */
@Component
public class OAuth2AuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    /**
     * Handles an OAuth2 authentication failure.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param exception authentication failure exception
     * @throws IOException if an I/O error occurs
     * @throws ServletException if servlet processing fails
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType("application/json");

        response.getWriter().write(
                "{\"message\":\"OAuth2 authentication failed\"}"
        );
    }
}