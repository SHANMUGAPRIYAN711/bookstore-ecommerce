package com.bookstore.book.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO used by authorized users to create a new book.
 *
 * <p>
 * The book cover image is uploaded separately through the storage
 * module. After the upload succeeds, the returned S3 object key
 * can be supplied through this DTO.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    /**
     * Book title.
     */
    @NotBlank(message = "Title is required")
    @Size(
            max = 255,
            message = "Title must not exceed 255 characters"
    )
    private String title;

    /**
     * International Standard Book Number.
     */
    @NotBlank(message = "ISBN is required")
    @Size(
            max = 20,
            message = "ISBN must not exceed 20 characters"
    )
    private String isbn;

    /**
     * Book author.
     */
    @NotBlank(message = "Author is required")
    @Size(
            max = 255,
            message = "Author must not exceed 255 characters"
    )
    private String author;

    /**
     * Book description.
     */
    @Size(
            max = 5000,
            message = "Description must not exceed 5000 characters"
    )
    private String description;

    /**
     * Book category.
     */
    @NotBlank(message = "Category is required")
    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    /**
     * Selling price.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price must be greater than zero"
    )
    private BigDecimal price;

    /**
     * Initial inventory quantity.
     */
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(
            message = "Stock quantity cannot be negative"
    )
    private Integer stockQuantity;

    /**
     * S3 object key of the book cover image.
     *
     * <p>
     * Example:
     * uploads/73c97b3e-cffe-4546-9bcc-3f597d5a8137.jpg
     * </p>
     */
    @Size(
            max = 500,
            message = "Image key must not exceed 500 characters"
    )
    private String imageKey;
}