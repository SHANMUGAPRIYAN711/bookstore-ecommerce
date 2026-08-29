package com.bookstore.wishlist.service;

import com.bookstore.wishlist.dto.WishlistItemResponse;

import java.util.List;
import java.util.UUID;

public interface WishlistService {

    List<WishlistItemResponse> getWishlist(UUID userId);

    WishlistItemResponse addToWishlist(
            UUID userId,
            UUID bookId
    );

    void removeFromWishlist(
            UUID userId,
            UUID bookId
    );
}