package com.bookstore.cart.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to change the quantity of an existing cart item.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    /**
     * New quantity requested for the cart item.
     */
    @Positive(message = "Quantity must be greater than zero")
    private int quantity;
}