package com.bookstore.cart.controller;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartItemResponse;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.cart.service.CartService;
import com.bookstore.security.JwtService;
import com.bookstore.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    private CartResponse createCartResponse() {

        CartItemResponse itemResponse =
                CartItemResponse.builder()
                        .id(cartItemId)
                        .bookId(bookId)
                        .bookTitle("Clean Code")
                        .imageUrl(
                                "https://example.com/clean-code.jpg"
                        )
                        .unitPrice(
                                new BigDecimal("45.00")
                        )
                        .quantity(2)
                        .subtotal(
                                new BigDecimal("90.00")
                        )
                        .build();

        return CartResponse.builder()
                .id(cartId)
                .items(List.of(itemResponse))
                .totalItems(2)
                .totalAmount(new BigDecimal("90.00"))
                .build();
    }

    // ============================================================
    // GET CART
    // ============================================================

    @Test
    void getCart_shouldReturnCart() throws Exception {

        CartResponse response =
                createCartResponse();

        when(cartService.getCart(userId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/cart")
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
                                        "Cart retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(cartId.toString())
                )
                .andExpect(
                        jsonPath(
                                "$.data.items[0].bookId"
                        ).value(bookId.toString())
                )
                .andExpect(
                        jsonPath(
                                "$.data.items[0].bookTitle"
                        ).value("Clean Code")
                )
                .andExpect(
                        jsonPath(
                                "$.data.items[0].quantity"
                        ).value(2)
                )
                .andExpect(
                        jsonPath(
                                "$.data.totalItems"
                        ).value(2)
                )
                .andExpect(
                        jsonPath(
                                "$.data.totalAmount"
                        ).value(90.00)
                );

        verify(cartService)
                .getCart(userId);
    }

    @Test
    void getCart_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(
                        get("/api/cart")
                                .param("userId", "invalid-uuid")
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(cartService, never())
                .getCart(any());
    }

    // ============================================================
    // ADD TO CART
    // ============================================================

    @Test
    void addToCart_shouldReturnCreated() throws Exception {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(2)
                        .build();

        CartResponse response =
                createCartResponse();

        when(cartService.addToCart(
                eq(userId),
                any(AddToCartRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/cart/items")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
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
                                        "Book added to cart successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(cartId.toString())
                )
                .andExpect(
                        jsonPath(
                                "$.data.totalAmount"
                        ).value(90.00)
                );

        verify(cartService).addToCart(
                eq(userId),
                any(AddToCartRequest.class)
        );
    }

    @Test
    void addToCart_shouldRejectInvalidRequest() throws Exception {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(null)
                        .quantity(0)
                        .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(cartService, never())
                .addToCart(
                        any(),
                        any(AddToCartRequest.class)
                );
    }

    @Test
    void addToCart_shouldRejectInvalidUserId() throws Exception {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(2)
                        .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(cartService, never())
                .addToCart(
                        any(),
                        any(AddToCartRequest.class)
                );
    }

    // ============================================================
    // UPDATE CART ITEM
    // ============================================================

    @Test
    void updateCartItem_shouldReturnUpdatedCart() throws Exception {

        UpdateCartItemRequest request =
                UpdateCartItemRequest.builder()
                        .quantity(5)
                        .build();

        CartResponse response =
                createCartResponse();

        response.getItems()
                .get(0)
                .setQuantity(5);

        response.getItems()
                .get(0)
                .setSubtotal(
                        new BigDecimal("225.00")
                );

        response.setTotalItems(5);
        response.setTotalAmount(
                new BigDecimal("225.00")
        );

        when(cartService.updateCartItem(
                eq(userId),
                eq(cartItemId),
                any(UpdateCartItemRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/cart/items/{itemId}",
                                cartItemId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath(
                                "$.data.items[0].quantity"
                        ).value(5)
                )
                .andExpect(
                        jsonPath(
                                "$.data.totalItems"
                        ).value(5)
                )
                .andExpect(
                        jsonPath(
                                "$.data.totalAmount"
                        ).value(225.00)
                );

        verify(cartService).updateCartItem(
                eq(userId),
                eq(cartItemId),
                any(UpdateCartItemRequest.class)
        );
    }

    @Test
    void updateCartItem_shouldRejectInvalidRequest() throws Exception {

        UpdateCartItemRequest request =
                UpdateCartItemRequest.builder()
                        .quantity(0)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/cart/items/{itemId}",
                                cartItemId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(cartService, never())
                .updateCartItem(
                        any(),
                        any(),
                        any(UpdateCartItemRequest.class)
                );
    }

    // ============================================================
    // REMOVE CART ITEM
    // ============================================================

    @Test
    void removeCartItem_shouldReturnSuccess() throws Exception {

        doNothing()
                .when(cartService)
                .removeCartItem(
                        userId,
                        cartItemId
                );

        mockMvc.perform(
                        delete(
                                "/api/cart/items/{itemId}",
                                cartItemId
                        )
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
                                        "Cart item removed successfully"
                                )
                );

        verify(cartService)
                .removeCartItem(
                        userId,
                        cartItemId
                );
    }

    @Test
    void removeCartItem_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/cart/items/{itemId}",
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

        verify(cartService, never())
                .removeCartItem(
                        any(),
                        any()
                );
    }

    // ============================================================
    // CLEAR CART
    // ============================================================

    @Test
    void clearCart_shouldReturnSuccess() throws Exception {

        doNothing()
                .when(cartService)
                .clearCart(userId);

        mockMvc.perform(
                        delete("/api/cart")
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
                                        "Cart cleared successfully"
                                )
                );

        verify(cartService)
                .clearCart(userId);
    }

    @Test
    void clearCart_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(
                        delete("/api/cart")
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(cartService, never())
                .clearCart(any());
    }
}