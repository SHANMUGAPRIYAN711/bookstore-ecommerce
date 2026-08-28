package com.bookstore.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a saved user address.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    /**
     * Address identifier.
     */
    private UUID id;

    /**
     * First address line.
     */
    private String addressLine1;

    /**
     * Optional second address line.
     */
    private String addressLine2;

    /**
     * City.
     */
    private String city;

    /**
     * State or province.
     */
    private String state;

    /**
     * Postal code.
     */
    private String postalCode;

    /**
     * Country.
     */
    private String country;

    /**
     * Indicates whether this is the default address.
     */
    private boolean defaultAddress;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last modification timestamp.
     */
    private Instant updatedAt;
}