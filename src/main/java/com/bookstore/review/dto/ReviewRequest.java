package com.bookstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO used by an authenticated customer to create or update
 * a review for a book.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    /**
     * Book being reviewed.
     */
    @NotNull(message = "Book ID is required")
    private UUID bookId;

    /**
     * Rating assigned to the book.
     */
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int rating;

    /**
     * Optional written review.
     */
    @Size(max = 2000, message = "Review must not exceed 2000 characters")
    private String comment;
}