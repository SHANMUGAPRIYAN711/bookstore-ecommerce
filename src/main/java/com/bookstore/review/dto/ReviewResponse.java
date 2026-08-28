package com.bookstore.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a customer review.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    /**
     * Review identifier.
     */
    private UUID id;

    /**
     * Book identifier.
     */
    private UUID bookId;

    /**
     * User identifier of the reviewer.
     */
    private UUID userId;

    /**
     * Display name of the reviewer.
     */
    private String reviewerName;

    /**
     * Rating from one to five.
     */
    private int rating;

    /**
     * Written review.
     */
    private String comment;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last modification timestamp.
     */
    private Instant updatedAt;
}