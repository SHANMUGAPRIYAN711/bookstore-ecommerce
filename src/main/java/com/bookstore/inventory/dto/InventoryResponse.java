package com.bookstore.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Response DTO representing the current inventory information
 * of a book.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    /**
     * Identifier of the book.
     */
    private UUID bookId;

    /**
     * Title of the book.
     */
    private String bookTitle;

    /**
     * Current number of units available in inventory.
     */
    private Integer stockQuantity;

    /**
     * Indicates whether the book is currently available
     * for purchase.
     */
    private boolean available;
}