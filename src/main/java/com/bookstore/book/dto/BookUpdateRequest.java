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
    @Size(max = 255)
    private String title;

    /**
     * Updated ISBN.
     */
    @Size(max = 20)
    private String isbn;

    /**
     * Updated author.
     */
    @Size(max = 255)
    private String author;

    /**
     * Updated description.
     */
    @Size(max = 5000)
    private String description;

    /**
     * Updated category.
     */
    @Size(max = 100)
    private String category;

    /**
     * Updated selling price.
     */
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    /**
     * Updated inventory quantity.
     */
    @PositiveOrZero
    private Integer stockQuantity;

    /**
     * Updated S3 image URL or object reference.
     */
    @Size(max = 1000)
    private String imageUrl;

    /**
     * Indicates whether the book is currently available for sale.
     */
    private Boolean active;
}