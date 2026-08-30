package com.bookstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles successful OAuth2 authentication.
 *
 * <p>
 * This handler is invoked by Spring Security after the OAuth2 provider
 * successfully authenticates the user.
 * </p>
 */
@Component
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    /**
     * Handles a successful OAuth2 authentication.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param authentication authenticated OAuth2 authentication
     * @throws IOException if an I/O error occurs
     * @throws ServletException if servlet processing fails
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        /*
         * OAuth2 authentication has completed successfully.
         *
         * The authenticated principal is available through:
         *
         * authentication.getPrincipal()
         *
         * JWT generation and application-user provisioning will be
         * added when the complete OAuth2 authentication flow is configured.
         */

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        response.getWriter().write(
                "{\"message\":\"OAuth2 authentication successful\"}"
        );
    }
}