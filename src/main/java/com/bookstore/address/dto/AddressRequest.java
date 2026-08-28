package com.bookstore.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to create or update a user's address.
 *
 * <p>
 * The authenticated user's ID is intentionally not accepted from
 * the client. The service obtains the user from the security context.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    /**
     * First address line.
     */
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String addressLine1;

    /**
     * Optional second address line.
     */
    @Size(max = 255)
    private String addressLine2;

    /**
     * City.
     */
    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    /**
     * State or province.
     */
    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    /**
     * Postal or ZIP code.
     */
    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    private String postalCode;

    /**
     * Country.
     */
    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    /**
     * Indicates whether this address should become the default address.
     */
    private boolean defaultAddress;
}