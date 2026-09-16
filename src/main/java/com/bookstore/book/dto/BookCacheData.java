package com.bookstore.book.dto;

import com.bookstore.common.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the book data stored in Redis.
 *
 * <p>
 * This DTO contains only persisted book information required to
 * reconstruct a {@link BookResponse}. Temporary values such as the
 * AWS S3 presigned image URL are intentionally not cached because
 * presigned URLs expire after a limited period.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCacheData {

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
     * S3 object key for the book cover.
     */
    private String imageKey;

    /**
     * Average customer rating.
     */
    private Double averageRating;

    /**
     * Number of reviews.
     */
    private Long reviewCount;

    /**
     * Current catalog status.
     */
    private BookStatus status;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last modification timestamp.
     */
    private Instant updatedAt;
}