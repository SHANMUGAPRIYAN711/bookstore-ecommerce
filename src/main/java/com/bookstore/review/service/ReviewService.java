package com.bookstore.review.service;

import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for review-related business operations.
 *
 * <p>
 * Defines the operations required to create, retrieve, update,
 * and delete customer reviews.
 * </p>
 */
public interface ReviewService {

    /**
     * Creates a new review for a book.
     *
     * @param userId authenticated user's identifier
     * @param request review creation request
     * @return created review
     */
    ReviewResponse createReview(
            UUID userId,
            ReviewRequest request
    );

    /**
     * Retrieves a review by its identifier.
     *
     * @param reviewId review identifier
     * @return review details
     */
    ReviewResponse getReviewById(UUID reviewId);

    /**
     * Retrieves reviews belonging to a particular book.
     *
     * @param bookId book identifier
     * @param page page number
     * @param size number of reviews per page
     * @param sortBy field used for sorting
     * @param sortDirection sorting direction
     * @return paginated reviews
     */
    Page<ReviewResponse> getReviewsByBook(
            UUID bookId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    /**
     * Updates an existing review.
     *
     * @param reviewId review identifier
     * @param userId authenticated user's identifier
     * @param request updated review information
     * @return updated review
     */
    ReviewResponse updateReview(
            UUID reviewId,
            UUID userId,
            ReviewRequest request
    );

    /**
     * Deletes an existing review.
     *
     * @param reviewId review identifier
     * @param userId authenticated user's identifier
     */
    void deleteReview(
            UUID reviewId,
            UUID userId
    );
}