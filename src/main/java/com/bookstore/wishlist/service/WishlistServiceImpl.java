package com.bookstore.wishlist.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.wishlist.dto.WishlistItemResponse;
import com.bookstore.wishlist.entity.WishlistItem;
import com.bookstore.wishlist.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Auditable(
            action = "GET_WISHLIST",
            entity = "WISHLIST"
    )
    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getWishlist(UUID userId) {

        validateUser(userId);

        return wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Auditable(
            action = "ADD_TO_WISHLIST",
            entity = "WISHLIST"
    )
    @Override
    public WishlistItemResponse addToWishlist(
            UUID userId,
            UUID bookId
    ) {

        User user = getUser(userId);

        Book book = getBook(bookId);

        boolean alreadyExists =
                wishlistItemRepository
                        .existsByUserIdAndBookId(
                                userId,
                                bookId
                        );

        if (alreadyExists) {

            throw new BadRequestException(
                    "Book is already in wishlist"
            );
        }

        WishlistItem wishlistItem =
                WishlistItem.builder()
                        .user(user)
                        .book(book)
                        .build();

        WishlistItem savedItem =
                wishlistItemRepository.save(wishlistItem);

        return mapToResponse(savedItem);
    }

    @Auditable(
            action = "REMOVE_FROM_WISHLIST",
            entity = "WISHLIST"
    )
    @Override
    public void removeFromWishlist(
            UUID userId,
            UUID bookId
    ) {

        WishlistItem wishlistItem =
                wishlistItemRepository
                        .findByUserIdAndBookId(
                                userId,
                                bookId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Book not found in wishlist"
                                )
                        );

        wishlistItemRepository.delete(wishlistItem);
    }

    private User getUser(UUID userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private void validateUser(UUID userId) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException(
                    "User not found"
            );
        }
    }

    private Book getBook(UUID bookId) {

        return bookRepository
                .findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );
    }

    private WishlistItemResponse mapToResponse(
            WishlistItem wishlistItem
    ) {

        Book book = wishlistItem.getBook();

        boolean available =
                book.getStatus() == BookStatus.ACTIVE
                        && book.getStockQuantity() > 0;

        return WishlistItemResponse.builder()
                .id(wishlistItem.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .imageUrl(book.getImageUrl())
                .available(available)
                .createdAt(wishlistItem.getCreatedAt())
                .build();
    }
}