package com.bookstore.wishlist.repository;

import com.bookstore.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, UUID> {

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<WishlistItem> findByUserIdAndBookId(
            UUID userId,
            UUID bookId
    );

    Optional<WishlistItem> findByIdAndUserId(
            UUID itemId,
            UUID userId
    );

    boolean existsByUserIdAndBookId(
            UUID userId,
            UUID bookId
    );
}