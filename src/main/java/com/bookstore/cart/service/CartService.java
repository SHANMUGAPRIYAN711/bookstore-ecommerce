package com.bookstore.cart.service;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID userId);

    CartResponse addToCart(
            UUID userId,
            AddToCartRequest request
    );

    CartResponse updateCartItem(
            UUID userId,
            UUID itemId,
            UpdateCartItemRequest request
    );

    void removeCartItem(
            UUID userId,
            UUID itemId
    );

    void clearCart(UUID userId);
}