package com.bookstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO representing the authenticated user's shopping cart.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    /**
     * Cart identifier.
     */
    private UUID id;

    /**
     * Items contained in the cart.
     */
    private List<CartItemResponse> items;

    /**
     * Total number of books in the cart.
     */
    private int totalItems;

    /**
     * Total monetary value of the cart.
     */
    private BigDecimal totalAmount;
}