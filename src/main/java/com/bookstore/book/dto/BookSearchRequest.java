package com.bookstore.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO containing optional filters used for searching and
 * filtering books in the catalog.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequest {

    /**
     * Search text applied to title, author, or ISBN.
     */
    private String keyword;

    /**
     * Category filter.
     */
    private String category;

    /**
     * Minimum price.
     */
    private BigDecimal minPrice;

    /**
     * Maximum price.
     */
    private BigDecimal maxPrice;

    /**
     * Minimum rating.
     */
    private Double minRating;

    /**
     * Requested page number.
     */
    @Min(value = 0, message = "Page must not be negative")
    @Builder.Default
    private int page = 0;

    /**
     * Number of records per page.
     */
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private int size = 20;

    /**
     * Field used for sorting.
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Sorting direction.
     */
    @Builder.Default
    private String sortDirection = "DESC";
}