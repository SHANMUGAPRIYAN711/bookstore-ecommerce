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
 * The image itself is not uploaded through this DTO. Image upload is
 * handled by the storage/S3 module and the resulting object URL or key
 * is associated with the book separately.
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
    @Size(max = 255)
    private String title;

    /**
     * International Standard Book Number.
     */
    @NotBlank(message = "ISBN is required")
    @Size(max = 20)
    private String isbn;

    /**
     * Book author.
     */
    @NotBlank(message = "Author is required")
    @Size(max = 255)
    private String author;

    /**
     * Book description.
     */
    @Size(max = 5000)
    private String description;

    /**
     * Book category.
     */
    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    /**
     * Selling price.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    /**
     * Initial inventory quantity.
     */
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    /**
     * S3 image URL or object reference.
     */
    @Size(max = 1000)
    private String imageUrl;
}