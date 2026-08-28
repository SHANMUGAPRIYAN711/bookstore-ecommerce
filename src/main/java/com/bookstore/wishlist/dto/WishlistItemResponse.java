package com.bookstore.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a book saved in a user's wishlist.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

    /**
     * Wishlist item identifier.
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
     * Book author.
     */
    private String author;

    /**
     * Current book price.
     */
    private BigDecimal price;

    /**
     * Book image URL.
     */
    private String imageUrl;

    /**
     * Indicates whether the book is currently available.
     */
    private boolean available;

    /**
     * Timestamp at which the item was added to the wishlist.
     */
    private Instant createdAt;
}