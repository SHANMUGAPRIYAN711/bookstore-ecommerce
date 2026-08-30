package com.bookstore.security;

import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads application users for Spring Security authentication.
 *
 * <p>
 * Spring Security calls this service when it needs to retrieve the
 * application user associated with an authentication identifier.
 * </p>
 *
 * <p>
 * In the Bookstore application, the user's email address is used as
 * the authentication identifier.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user using their email address.
     *
     * @param email user's email address
     * @return Spring Security representation of the user
     * @throws UsernameNotFoundException when no user exists
     *         with the supplied email address
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(
                        user.getPassword() != null
                                ? user.getPassword()
                                : ""
                )
                .roles(user.getRole().name())
                .build();
    }
}