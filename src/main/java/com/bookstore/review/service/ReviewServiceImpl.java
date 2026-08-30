package com.bookstore.review.service;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ForbiddenException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.entity.Review;
import com.bookstore.review.repository.ReviewRepository;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of review-related business operations.
 *
 * <p>
 * Handles review creation, retrieval, updating, deletion,
 * ownership validation, duplicate-review prevention,
 * pagination, and sorting.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new review for a book.
     *
     * <p>
     * A user can submit only one review for a particular book.
     * </p>
     *
     * @param userId authenticated user's identifier
     * @param request review creation request
     * @return created review response
     */
    @Override
    public ReviewResponse createReview(
            UUID userId,
            ReviewRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: "
                                        + request.getBookId()
                        ));

        if (reviewRepository.existsByUserIdAndBookId(
                userId,
                request.getBookId())) {

            throw new BadRequestException(
                    "User has already reviewed this book"
            );
        }

        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        return mapToResponse(savedReview);
    }

    /**
     * Retrieves a review by its identifier.
     *
     * @param reviewId review identifier
     * @return review response
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found with id: " + reviewId
                        ));

        return mapToResponse(review);
    }

    /**
     * Retrieves all reviews for a particular book.
     *
     * @param bookId book identifier
     * @param page page number
     * @param size number of reviews per page
     * @param sortBy field used for sorting
     * @param sortDirection sorting direction
     * @return paginated review responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByBook(
            UUID bookId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException(
                    "Book not found with id: " + bookId
            );
        }

        Sort sort = buildSort(sortBy, sortDirection);

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return reviewRepository
                .findByBookId(bookId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Updates an existing review.
     *
     * <p>
     * Only the user who originally created the review
     * can update it.
     * </p>
     *
     * @param reviewId review identifier
     * @param userId authenticated user's identifier
     * @param request updated review information
     * @return updated review response
     */
    @Override
    public ReviewResponse updateReview(
            UUID reviewId,
            UUID userId,
            ReviewRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found with id: " + reviewId
                        ));

        validateOwnership(review, userId);

        if (request.getBookId() != null
                && !request.getBookId()
                .equals(review.getBook().getId())) {

            throw new BadRequestException(
                    "Book cannot be changed while updating a review"
            );
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview =
                reviewRepository.save(review);

        return mapToResponse(updatedReview);
    }

    /**
     * Deletes an existing review.
     *
     * <p>
     * Only the user who originally created the review
     * can delete it.
     * </p>
     *
     * @param reviewId review identifier
     * @param userId authenticated user's identifier
     */
    @Override
    public void deleteReview(
            UUID reviewId,
            UUID userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found with id: " + reviewId
                        ));

        validateOwnership(review, userId);

        reviewRepository.delete(review);
    }

    /**
     * Validates that the supplied user owns the review.
     *
     * @param review review being modified
     * @param userId authenticated user's identifier
     */
    private void validateOwnership(
            Review review,
            UUID userId) {

        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(
                    "You are not authorized to modify this review"
            );
        }
    }

    /**
     * Builds pagination and sorting information.
     *
     * <p>
     * Reviews are sorted by creation time in descending order
     * by default.
     * </p>
     *
     * @param sortBy field used for sorting
     * @param sortDirection sorting direction
     * @return configured Sort object
     */
    private Sort buildSort(
            String sortBy,
            String sortDirection) {

        String field =
                (sortBy == null || sortBy.isBlank())
                        ? "createdAt"
                        : sortBy;

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    /**
     * Converts a Review entity into a response DTO.
     *
     * @param review review entity
     * @return review response
     */
    private ReviewResponse mapToResponse(
            Review review) {

        String reviewerName =
                review.getUser().getFirstName()
                        + " "
                        + review.getUser().getLastName();

        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .userId(review.getUser().getId())
                .reviewerName(reviewerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}