package com.bookstore.review.controller;

import com.bookstore.common.constants.ApiConstants;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.service.ReviewService;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller responsible for customer review operations.
 */
@RestController
@RequestMapping(ApiConstants.REVIEWS_PATH)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    /**
     * Creates a review for a book.
     *
     * @param request review information
     * @param authentication authenticated user's security information
     * @return created review
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        ReviewResponse response =
                reviewService.createReview(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves a review by its identifier.
     *
     * @param id review identifier
     * @return review details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                reviewService.getReviewById(id)
        );
    }

    /**
     * Retrieves all reviews for a particular book.
     *
     * @param bookId book identifier
     * @param page page number
     * @param size page size
     * @param sortBy field used for sorting
     * @param sortDirection sorting direction
     * @return paginated reviews
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByBook(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        return ResponseEntity.ok(
                reviewService.getReviewsByBook(
                        bookId,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /**
     * Updates a review owned by the authenticated user.
     *
     * @param id review identifier
     * @param request updated review information
     * @param authentication authenticated user's security information
     * @return updated review
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                reviewService.updateReview(
                        id,
                        userId,
                        request
                )
        );
    }

    /**
     * Deletes a review owned by the authenticated user.
     *
     * @param id review identifier
     * @param authentication authenticated user's security information
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        reviewService.deleteReview(id, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the application user associated with
     * the authenticated security principal.
     *
     * <p>
     * Spring Security stores the user's email as the principal name.
     * The email is therefore used to locate the application's User
     * entity and obtain its UUID.
     * </p>
     *
     * @param authentication current authentication
     * @return authenticated user's UUID
     */
    private UUID getAuthenticatedUserId(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user no longer exists"
                        ));

        return user.getId();
    }
}