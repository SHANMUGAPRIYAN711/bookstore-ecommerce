package com.bookstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO representing an individual item in a shopping cart.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    /**
     * Cart item identifier.
     */
    private UUID id;

    /**
     * Book identifier.
     */
    private UUID bookId;

    /**
     * Book title.
     */
    private String bookTitle;

    /**
     * Book image URL.
     */
    private String imageUrl;

    /**
     * Price of one copy at the time of cart retrieval.
     */
    private BigDecimal unitPrice;

    /**
     * Number of copies.
     */
    private int quantity;

    /**
     * Total price for this cart item.
     */
    private BigDecimal subtotal;
}