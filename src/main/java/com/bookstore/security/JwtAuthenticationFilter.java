package com.bookstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Servlet filter responsible for processing JWT authentication on incoming
 * HTTP requests.
 *
 * <p>The filter reads the JWT from the Authorization header, validates the
 * token, loads the corresponding user details, and places an authenticated
 * Authentication object into the Spring Security context.</p>
 *
 * <p>The filter executes once for each HTTP request.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Processes an incoming HTTP request and attempts to authenticate the
     * request using the JWT supplied in the Authorization header.
     *
     * <p>If no Bearer token is present, the request continues through the
     * remaining filter chain without authentication being established by
     * this filter.</p>
     *
     * <p>If a valid JWT is found and the corresponding user is not already
     * authenticated, the user's authentication is stored in the
     * SecurityContext.</p>
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain chain of remaining security filters
     * @throws ServletException when servlet processing fails
     * @throws IOException when request or response processing fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authorizationHeader.substring(7);

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    log.info(
                            "JWT AUTHENTICATED | username={} | authorities={}",
                            userDetails.getUsername(),
                            userDetails.getAuthorities()
                    );
                }
            }

        } catch (Exception exception) {

        log.error(
                "JWT AUTHENTICATION FAILED | URI={} | error={}",
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );
    }

        filterChain.doFilter(request, response);
    }
}