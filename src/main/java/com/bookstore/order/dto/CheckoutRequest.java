package com.bookstore.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO used to initiate checkout for the authenticated user's cart.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    /**
     * Address to which the order should be delivered.
     */
    @NotNull(message = "Shipping address is required")
    private UUID shippingAddressId;
}