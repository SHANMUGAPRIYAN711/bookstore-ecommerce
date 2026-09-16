package com.bookstore.book.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO used to update an existing book.
 *
 * <p>
 * Only editable catalog information is exposed through this DTO.
 * The book cover is represented by its S3 object key.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateRequest {

    /**
     * Updated title.
     */
    @Size(
            max = 255,
            message = "Title must not exceed 255 characters"
    )
    private String title;

    /**
     * Updated ISBN.
     */
    @Size(
            max = 20,
            message = "ISBN must not exceed 20 characters"
    )
    private String isbn;

    /**
     * Updated author.
     */
    @Size(
            max = 255,
            message = "Author must not exceed 255 characters"
    )
    private String author;

    /**
     * Updated description.
     */
    @Size(
            max = 5000,
            message = "Description must not exceed 5000 characters"
    )
    private String description;

    /**
     * Updated category.
     */
    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    /**
     * Updated selling price.
     */
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price must be greater than zero"
    )
    private BigDecimal price;

    /**
     * Updated inventory quantity.
     */
    @PositiveOrZero(
            message = "Stock quantity cannot be negative"
    )
    private Integer stockQuantity;

    /**
     * S3 object key of the updated book cover.
     */
    @Size(
            max = 500,
            message = "Image key must not exceed 500 characters"
    )
    private String imageKey;

    /**
     * Indicates whether the book is currently available for sale.
     */
    private Boolean active;
}