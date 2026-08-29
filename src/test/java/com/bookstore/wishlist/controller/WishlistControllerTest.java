package com.bookstore.wishlist.controller;

import com.bookstore.security.JwtService;
import com.bookstore.user.service.UserService;
import com.bookstore.wishlist.dto.WishlistItemResponse;
import com.bookstore.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private WishlistService wishlistService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();
    private final UUID wishlistItemId = UUID.randomUUID();

    private WishlistItemResponse createWishlistItemResponse() {

        return WishlistItemResponse.builder()
                .id(wishlistItemId)
                .bookId(bookId)
                .bookTitle("Clean Code")
                .author("Robert Martin")
                .price(new BigDecimal("45.00"))
                .imageUrl(
                        "https://example.com/clean-code.jpg"
                )
                .available(true)
                .createdAt(Instant.now())
                .build();
    }

    // ============================================================
    // GET WISHLIST
    // ============================================================

    @Test
    void getWishlist_shouldReturnWishlist() throws Exception {

        WishlistItemResponse response =
                createWishlistItemResponse();

        when(wishlistService.getWishlist(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/wishlist")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Wishlist retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].id"
                        ).value(
                                wishlistItemId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].bookId"
                        ).value(
                                bookId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].bookTitle"
                        ).value("Clean Code")
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].author"
                        ).value("Robert Martin")
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].price"
                        ).value(45.00)
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].available"
                        ).value(true)
                );

        verify(wishlistService)
                .getWishlist(userId);
    }

    @Test
    void getWishlist_shouldReturnEmptyWishlist()
            throws Exception {

        when(wishlistService.getWishlist(userId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/wishlist")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(0)
                );

        verify(wishlistService)
                .getWishlist(userId);
    }

    @Test
    void getWishlist_shouldRejectInvalidUserId()
            throws Exception {

        mockMvc.perform(
                        get("/api/wishlist")
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(wishlistService, never())
                .getWishlist(any());
    }

    // ============================================================
    // ADD TO WISHLIST
    // ============================================================

    @Test
    void addToWishlist_shouldReturnCreated()
            throws Exception {

        WishlistItemResponse response =
                createWishlistItemResponse();

        when(wishlistService.addToWishlist(
                userId,
                bookId
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/wishlist/books/{bookId}", bookId)
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Book added to wishlist successfully"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.data.id"
                        ).value(
                                wishlistItemId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.data.bookId"
                        ).value(
                                bookId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.data.bookTitle"
                        ).value("Clean Code")
                )
                .andExpect(
                        jsonPath(
                                "$.data.available"
                        ).value(true)
                );

        verify(wishlistService)
                .addToWishlist(
                        userId,
                        bookId
                );
    }

    @Test
    void addToWishlist_shouldRejectInvalidUserId()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/wishlist/books/{bookId}",
                                bookId
                        )
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(wishlistService, never())
                .addToWishlist(
                        any(),
                        any()
                );
    }

    @Test
    void addToWishlist_shouldRejectInvalidBookId()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/wishlist/books/{bookId}",
                                "invalid-uuid"
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(wishlistService, never())
                .addToWishlist(
                        any(),
                        any()
                );
    }

    // ============================================================
    // REMOVE FROM WISHLIST
    // ============================================================

    @Test
    void removeFromWishlist_shouldReturnSuccess()
            throws Exception {

        doNothing()
                .when(wishlistService)
                .removeFromWishlist(
                        userId,
                        bookId
                );

        mockMvc.perform(
                        delete(
                                "/api/wishlist/books/{bookId}",
                                bookId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Book removed from wishlist successfully"
                                )
                );

        verify(wishlistService)
                .removeFromWishlist(
                        userId,
                        bookId
                );
    }

    @Test
    void removeFromWishlist_shouldRejectInvalidBookId()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/wishlist/books/{bookId}",
                                "invalid-uuid"
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(wishlistService, never())
                .removeFromWishlist(
                        any(),
                        any()
                );
    }

    @Test
    void removeFromWishlist_shouldRejectInvalidUserId()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/wishlist/books/{bookId}",
                                bookId
                        )
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(wishlistService, never())
                .removeFromWishlist(
                        any(),
                        any()
                );
    }
}