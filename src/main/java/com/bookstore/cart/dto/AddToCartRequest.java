package com.bookstore.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO used to add a book to the authenticated user's cart.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    /**
     * Identifier of the book being added.
     */
    @NotNull(message = "Book ID is required")
    private UUID bookId;

    /**
     * Number of copies requested.
     */
    @Positive(message = "Quantity must be greater than zero")
    private int quantity;
}