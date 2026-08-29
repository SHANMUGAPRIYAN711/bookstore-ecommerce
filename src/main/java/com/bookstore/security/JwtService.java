package com.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Provides JWT-related operations for the Bookstore E-Commerce application.
 *
 * <p>This service is responsible for generating JSON Web Tokens, extracting
 * claims from tokens, and validating token integrity and expiration.</p>
 *
 * <p>The JWT secret and expiration period are externalized through the
 * application configuration and must never be hardcoded in source code.</p>
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationTime;

    /**
     * Creates the JWT service using the configured secret and expiration time.
     *
     * @param secret configured JWT signing secret
     * @param expirationTime JWT validity period in milliseconds
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationTime) {

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationTime = expirationTime;
    }

    /**
     * Generates a JWT for the specified username.
     *
     * @param username username that will be stored as the JWT subject
     * @return generated signed JWT
     */
    public String generateToken(String username) {

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + expirationTime
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the username from the JWT subject claim.
     *
     * @param token JWT whose subject should be extracted
     * @return username stored in the token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates the JWT against the supplied username.
     *
     * <p>The token is considered valid when its subject matches the supplied
     * username and its expiration time has not been reached.</p>
     *
     * @param token JWT to validate
     * @param username expected username
     * @return true when the token is valid; otherwise false
     */
    public boolean isTokenValid(String token, String username) {

        try {
            String tokenUsername = extractUsername(token);

            return tokenUsername.equals(username)
                    && !isTokenExpired(token);

        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Checks whether the supplied JWT has expired.
     *
     * @param token JWT to check
     * @return true when the token has expired; otherwise false
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the JWT.
     *
     * @param token JWT whose expiration claim should be extracted
     * @return token expiration date
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Parses and returns all claims contained in the supplied JWT.
     *
     * @param token JWT to parse
     * @return claims contained in the token
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}