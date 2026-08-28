package com.bookstore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used when registering a new local bookstore user account.
 *
 * <p>
 * This DTO contains only client-provided registration information.
 * Password encoding and persistence are handled by the service layer.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    /**
     * User's first name.
     *
     * @return first name supplied by the client
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * User's last name.
     *
     * @return last name supplied by the client
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * Email address used for account identification.
     *
     * @return user's email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Raw password supplied during registration.
     *
     * <p>
     * The service layer must encode this password before storing it.
     * </p>
     *
     * @return raw registration password
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must contain between 8 and 100 characters"
    )
    private String password;

    /**
     * Optional phone number associated with the account.
     *
     * @return phone number
     */
    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    private String phoneNumber;
}