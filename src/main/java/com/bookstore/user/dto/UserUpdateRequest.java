package com.bookstore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to update editable profile information of an
 * authenticated bookstore user.
 *
 * <p>
 * Authentication credentials, roles, account status, and OAuth provider
 * information are intentionally excluded from this DTO.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * Updated first name.
     *
     * @return updated first name
     */
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * Updated last name.
     *
     * @return updated last name
     */
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * Updated email address.
     *
     * @return updated email address
     */
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Updated phone number.
     *
     * @return updated phone number
     */
    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    private String phoneNumber;
}