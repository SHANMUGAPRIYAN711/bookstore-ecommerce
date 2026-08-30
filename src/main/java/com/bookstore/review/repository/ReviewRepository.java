package com.bookstore.review.repository;

import com.bookstore.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository responsible for persistence operations related to reviews.
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Retrieves all reviews written for a specific book.
     *
     * @param bookId book identifier
     * @param pageable pagination and sorting information
     * @return paginated reviews for the book
     */
    Page<Review> findByBookId(UUID bookId, Pageable pageable);

    /**
     * Finds a review written by a specific user for a specific book.
     *
     * <p>
     * This is used to enforce the business rule that a user can
     * submit only one review for a particular book.
     * </p>
     *
     * @param userId user identifier
     * @param bookId book identifier
     * @return matching review if it exists
     */
    Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);

    /**
     * Checks whether a user has already reviewed a particular book.
     *
     * @param userId user identifier
     * @param bookId book identifier
     * @return true when a review already exists
     */
    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);
}