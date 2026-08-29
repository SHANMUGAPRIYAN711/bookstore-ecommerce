package com.bookstore.cart.controller;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.cart.service.CartService;
import com.bookstore.common.constants.ApiConstants;
import com.bookstore.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.CART_PATH)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestParam UUID userId
    ) {

        CartResponse response =
                cartService.getCart(userId);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .success(true)
                        .message("Cart retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestParam UUID userId,
            @Valid @RequestBody AddToCartRequest request
    ) {

        CartResponse response =
                cartService.addToCart(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<CartResponse>builder()
                                .success(true)
                                .message("Book added to cart successfully")
                                .data(response)
                                .build()
                );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @RequestParam UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        CartResponse response =
                cartService.updateCartItem(
                        userId,
                        itemId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .success(true)
                        .message("Cart item updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @RequestParam UUID userId,
            @PathVariable UUID itemId
    ) {

        cartService.removeCartItem(
                userId,
                itemId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cart item removed successfully")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestParam UUID userId
    ) {

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cart cleared successfully")
                        .data(null)
                        .build()
        );
    }
}