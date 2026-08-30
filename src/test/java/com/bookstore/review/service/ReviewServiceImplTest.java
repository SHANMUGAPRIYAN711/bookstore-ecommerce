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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID userId;
    private UUID anotherUserId;
    private UUID bookId;
    private UUID anotherBookId;
    private UUID reviewId;

    private User user;
    private User anotherUser;
    private Book book;
    private Book anotherBook;
    private Review review;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        anotherBookId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        user.setId(userId);

        anotherUser = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .build();

        anotherUser.setId(anotherUserId);

        book = Book.builder()
                .title("Clean Code")
                .build();

        book.setId(bookId);

        anotherBook = Book.builder()
                .title("Effective Java")
                .build();

        anotherBook.setId(anotherBookId);

        review = Review.builder()
                .user(user)
                .book(book)
                .rating(5)
                .comment("Excellent book")
                .build();

        review.setId(reviewId);
    }

    @Test
    void createReview_shouldCreateSuccessfully() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Excellent book")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByUserIdAndBookId(
                userId,
                bookId))
                .thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenReturn(review);

        ReviewResponse response =
                reviewService.createReview(userId, request);

        assertNotNull(response);
        assertEquals(reviewId, response.getId());
        assertEquals(bookId, response.getBookId());
        assertEquals(userId, response.getUserId());
        assertEquals("John Doe", response.getReviewerName());
        assertEquals(5, response.getRating());
        assertEquals("Excellent book", response.getComment());

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_shouldThrowWhenUserDoesNotExist() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Excellent book")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.createReview(userId, request)
        );

        verify(bookRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowWhenBookDoesNotExist() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Excellent book")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.createReview(userId, request)
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowWhenDuplicateReviewExists() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Excellent book")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByUserIdAndBookId(
                userId,
                bookId))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> reviewService.createReview(userId, request)
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewById_shouldReturnReview() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        ReviewResponse response =
                reviewService.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(reviewId, response.getId());
        assertEquals(bookId, response.getBookId());
        assertEquals(userId, response.getUserId());
        assertEquals(5, response.getRating());
        assertEquals("John Doe", response.getReviewerName());
    }

    @Test
    void getReviewById_shouldThrowWhenReviewDoesNotExist() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.getReviewById(reviewId)
        );
    }

    @Test
    void getReviewsByBook_shouldReturnPaginatedReviews() {

        when(bookRepository.existsById(bookId))
                .thenReturn(true);

        Page<Review> reviewPage =
                new PageImpl<>(List.of(review));

        when(reviewRepository.findByBookId(
                eq(bookId),
                any(Pageable.class)))
                .thenReturn(reviewPage);

        Page<ReviewResponse> response =
                reviewService.getReviewsByBook(
                        bookId,
                        0,
                        10,
                        "createdAt",
                        "DESC"
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(reviewId, response.getContent()
                .get(0)
                .getId());

        verify(reviewRepository)
                .findByBookId(eq(bookId), any(Pageable.class));
    }

    @Test
    void getReviewsByBook_shouldThrowWhenBookDoesNotExist() {

        when(bookRepository.existsById(bookId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.getReviewsByBook(
                        bookId,
                        0,
                        10,
                        null,
                        null
                )
        );

        verify(reviewRepository, never())
                .findByBookId(any(), any());
    }

    @Test
    void updateReview_shouldUpdateSuccessfully() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(4)
                .comment("Updated review")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ReviewResponse response =
                reviewService.updateReview(
                        reviewId,
                        userId,
                        request
                );

        assertNotNull(response);
        assertEquals(4, response.getRating());
        assertEquals("Updated review", response.getComment());

        verify(reviewRepository).save(review);
    }

    @Test
    void updateReview_shouldThrowWhenReviewDoesNotExist() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(4)
                .comment("Updated review")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        userId,
                        request
                )
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_shouldThrowWhenUserDoesNotOwnReview() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(4)
                .comment("Updated review")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ForbiddenException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        anotherUserId,
                        request
                )
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_shouldThrowWhenChangingBook() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(anotherBookId)
                .rating(4)
                .comment("Updated review")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                BadRequestException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        userId,
                        request
                )
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_shouldAllowSameBook() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(3)
                .comment("Updated comment")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ReviewResponse response =
                reviewService.updateReview(
                        reviewId,
                        userId,
                        request
                );

        assertEquals(3, response.getRating());
        assertEquals("Updated comment", response.getComment());

        verify(reviewRepository).save(review);
    }

    @Test
    void deleteReview_shouldDeleteSuccessfully() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(
                reviewId,
                userId
        );

        verify(reviewRepository)
                .delete(review);
    }

    @Test
    void deleteReview_shouldThrowWhenReviewDoesNotExist() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.deleteReview(
                        reviewId,
                        userId
                )
        );

        verify(reviewRepository, never())
                .delete(any());
    }

    @Test
    void deleteReview_shouldThrowWhenUserDoesNotOwnReview() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ForbiddenException.class,
                () -> reviewService.deleteReview(
                        reviewId,
                        anotherUserId
                )
        );

        verify(reviewRepository, never())
                .delete(any());
    }

    @Test
    void createReview_shouldMapReviewerNameCorrectly() {

        ReviewRequest request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(4)
                .comment("Good book")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByUserIdAndBookId(
                userId,
                bookId))
                .thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenReturn(review);

        ReviewResponse response =
                reviewService.createReview(
                        userId,
                        request
                );

        assertEquals(
                "John Doe",
                response.getReviewerName()
        );
    }

    @Test
    void updateReview_shouldNotAccessRepositoryWhenOwnershipFails() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ForbiddenException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        anotherUserId,
                        ReviewRequest.builder()
                                .bookId(bookId)
                                .rating(5)
                                .comment("Not allowed")
                                .build()
                )
        );

        verify(reviewRepository, never())
                .save(any());
    }

    @Test
    void deleteReview_shouldNotDeleteWhenOwnershipFails() {

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ForbiddenException.class,
                () -> reviewService.deleteReview(
                        reviewId,
                        anotherUserId
                )
        );

        verify(reviewRepository, never())
                .delete(any());
    }
}