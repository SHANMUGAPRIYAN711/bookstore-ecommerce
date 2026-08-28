package com.bookstore.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a book returned by the bookstore REST API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    /**
     * Book identifier.
     */
    private UUID id;

    /**
     * Book title.
     */
    private String title;

    /**
     * ISBN.
     */
    private String isbn;

    /**
     * Author.
     */
    private String author;

    /**
     * Book description.
     */
    private String description;

    /**
     * Book category.
     */
    private String category;

    /**
     * Selling price.
     */
    private BigDecimal price;

    /**
     * Current inventory quantity.
     */
    private Integer stockQuantity;

    /**
     * S3 image URL.
     */
    private String imageUrl;

    /**
     * Average customer rating.
     */
    private Double averageRating;

    /**
     * Number of reviews.
     */
    private Long reviewCount;

    /**
     * Indicates whether the book is available for purchase.
     */
    private boolean active;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last modification timestamp.
     */
    private Instant updatedAt;
}