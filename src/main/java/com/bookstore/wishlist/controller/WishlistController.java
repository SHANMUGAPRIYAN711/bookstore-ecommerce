package com.bookstore.wishlist.controller;

import com.bookstore.common.constants.ApiConstants;
import com.bookstore.common.dto.ApiResponse;
import com.bookstore.wishlist.dto.WishlistItemResponse;
import com.bookstore.wishlist.service.WishlistService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.WISHLIST_PATH)
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist(
            @RequestParam UUID userId
    ) {

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<WishlistItemResponse>>builder()
                        .success(true)
                        .message("Wishlist retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @RequestParam UUID userId,
            @PathVariable @NotNull UUID bookId
    ) {

        WishlistItemResponse response =
                wishlistService.addToWishlist(
                        userId,
                        bookId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<WishlistItemResponse>builder()
                                .success(true)
                                .message(
                                        "Book added to wishlist successfully"
                                )
                                .data(response)
                                .build()
                );
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @RequestParam UUID userId,
            @PathVariable @NotNull UUID bookId
    ) {

        wishlistService.removeFromWishlist(
                userId,
                bookId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(
                                "Book removed from wishlist successfully"
                        )
                        .data(null)
                        .build()
        );
    }
}