package com.bookstore.review.controller;

import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.service.ReviewService;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.bookstore.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UUID userId;
    private UUID bookId;
    private UUID reviewId;

    private User user;
    private ReviewRequest request;
    private ReviewResponse response;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        user.setId(userId);

        request = ReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Excellent book")
                .build();

        response = ReviewResponse.builder()
                .id(reviewId)
                .bookId(bookId)
                .userId(userId)
                .reviewerName("John Doe")
                .rating(5)
                .comment("Excellent book")
                .build();
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void createReview_shouldReturnCreated() throws Exception {

        when(userRepository.findByEmailIgnoreCase("john@example.com"))
                .thenReturn(Optional.of(user));

        when(reviewService.createReview(eq(userId), any(ReviewRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/reviews")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.reviewerName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Excellent book"));

        verify(userRepository)
                .findByEmailIgnoreCase("john@example.com");

        verify(reviewService)
                .createReview(eq(userId), any(ReviewRequest.class));
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void createReview_shouldReturnBadRequestForInvalidRating()
            throws Exception {

        ReviewRequest invalidRequest =
                ReviewRequest.builder()
                        .bookId(bookId)
                        .rating(6)
                        .comment("Invalid rating")
                        .build();

        mockMvc.perform(
                        post("/api/reviews")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void createReview_shouldReturnBadRequestWhenBookIdMissing()
            throws Exception {

        ReviewRequest invalidRequest =
                ReviewRequest.builder()
                        .rating(5)
                        .comment("Missing book")
                        .build();

        mockMvc.perform(
                        post("/api/reviews")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getReviewById_shouldReturnOk() throws Exception {

        when(reviewService.getReviewById(reviewId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/reviews/{id}", reviewId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.rating")
                        .value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Excellent book"));

        verify(reviewService)
                .getReviewById(reviewId);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getReviewsByBook_shouldReturnOk() throws Exception {

        Page<ReviewResponse> page =
                new PageImpl<>(List.of(response));

        when(reviewService.getReviewsByBook(
                eq(bookId),
                eq(0),
                eq(10),
                eq("createdAt"),
                eq("DESC")
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/reviews/book/{bookId}", bookId)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "createdAt")
                                .param("sortDirection", "DESC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(reviewId.toString()));

        verify(reviewService)
                .getReviewsByBook(
                        bookId,
                        0,
                        10,
                        "createdAt",
                        "DESC"
                );
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getReviewsByBook_shouldUseDefaultPagination() throws Exception {

        Page<ReviewResponse> page =
                new PageImpl<>(List.of(response));

        when(reviewService.getReviewsByBook(
                eq(bookId),
                eq(0),
                eq(10),
                isNull(),
                isNull()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/reviews/book/{bookId}", bookId)
                )
                .andExpect(status().isOk());

        verify(reviewService)
                .getReviewsByBook(
                        bookId,
                        0,
                        10,
                        null,
                        null
                );
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void updateReview_shouldReturnOk() throws Exception {

        when(userRepository.findByEmailIgnoreCase("john@example.com"))
                .thenReturn(Optional.of(user));

        ReviewResponse updatedResponse =
                ReviewResponse.builder()
                        .id(reviewId)
                        .bookId(bookId)
                        .userId(userId)
                        .reviewerName("John Doe")
                        .rating(4)
                        .comment("Updated review")
                        .build();

        when(reviewService.updateReview(
                eq(reviewId),
                eq(userId),
                any(ReviewRequest.class)
        )).thenReturn(updatedResponse);

        ReviewRequest updateRequest =
                ReviewRequest.builder()
                        .bookId(bookId)
                        .rating(4)
                        .comment("Updated review")
                        .build();

        mockMvc.perform(
                        put("/api/reviews/{id}", reviewId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment")
                        .value("Updated review"));

        verify(reviewService)
                .updateReview(
                        eq(reviewId),
                        eq(userId),
                        any(ReviewRequest.class)
                );
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void deleteReview_shouldReturnNoContent() throws Exception {

        when(userRepository.findByEmailIgnoreCase("john@example.com"))
                .thenReturn(Optional.of(user));

        doNothing()
                .when(reviewService)
                .deleteReview(reviewId, userId);

        mockMvc.perform(
                        delete("/api/reviews/{id}", reviewId)
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(reviewService)
                .deleteReview(reviewId, userId);
    }

    @Test
    void createReview_shouldRequireAuthentication()
            throws Exception {

        mockMvc.perform(
                        post("/api/reviews")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
        verifyNoInteractions(userRepository);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void updateReview_shouldRequireValidRequest()
            throws Exception {

        ReviewRequest invalidRequest =
                ReviewRequest.builder()
                        .bookId(bookId)
                        .rating(0)
                        .comment("Invalid")
                        .build();

        mockMvc.perform(
                        put("/api/reviews/{id}", reviewId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void createReview_shouldReturnUnauthorizedWhenUserNotFound()
            throws Exception {

        when(userRepository.findByEmailIgnoreCase("john@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/api/reviews")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().is5xxServerError());

        verify(userRepository)
                .findByEmailIgnoreCase("john@example.com");

        verifyNoInteractions(reviewService);
    }
}