package com.bookstore.wishlist.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private UUID userId;
    private UUID bookId;
    private UUID wishlistItemId;

    private User user;
    private Book book;
    private WishlistItem wishlistItem;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        wishlistItemId = UUID.randomUUID();

        user = User.builder()
                .firstName("Sauvik")
                .lastName("Nandi")
                .email("sauvik@example.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .build();

        user.setId(userId);

        book = Book.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("A handbook of agile software craftsmanship")
                .category("Programming")
                .price(new BigDecimal("45.00"))
                .stockQuantity(10)
                .imageUrl("https://example.com/clean-code.jpg")
                .status(BookStatus.ACTIVE)
                .build();

        book.setId(bookId);

        wishlistItem = WishlistItem.builder()
                .user(user)
                .book(book)
                .build();

        wishlistItem.setId(wishlistItemId);
    }

    // ============================================================
    // GET WISHLIST
    // ============================================================

    @Test
    void getWishlist_shouldReturnWishlistSuccessfully() {

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wishlistItem));

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        assertNotNull(response);
        assertEquals(1, response.size());

        WishlistItemResponse item = response.get(0);

        assertEquals(wishlistItemId, item.getId());
        assertEquals(bookId, item.getBookId());
        assertEquals("Clean Code", item.getBookTitle());
        assertEquals("Robert Martin", item.getAuthor());
        assertEquals(new BigDecimal("45.00"), item.getPrice());
        assertEquals(
                "https://example.com/clean-code.jpg",
                item.getImageUrl()
        );
        assertTrue(item.isAvailable());

        verify(userRepository).existsById(userId);

        verify(wishlistItemRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getWishlist_shouldReturnEmptyWishlist() {

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(userRepository).existsById(userId);

        verify(wishlistItemRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getWishlist_shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.existsById(userId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.getWishlist(userId)
        );

        verify(userRepository).existsById(userId);

        verify(wishlistItemRepository, never())
                .findByUserIdOrderByCreatedAtDesc(any());
    }

    // ============================================================
    // ADD TO WISHLIST
    // ============================================================

    @Test
    void addToWishlist_shouldAddBookSuccessfully() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(wishlistItemRepository.existsByUserIdAndBookId(
                userId,
                bookId
        )).thenReturn(false);

        when(wishlistItemRepository.save(
                any(WishlistItem.class)
        )).thenReturn(wishlistItem);

        WishlistItemResponse response =
                wishlistService.addToWishlist(
                        userId,
                        bookId
                );

        assertNotNull(response);
        assertEquals(wishlistItemId, response.getId());
        assertEquals(bookId, response.getBookId());
        assertEquals("Clean Code", response.getBookTitle());
        assertEquals("Robert Martin", response.getAuthor());
        assertEquals(
                new BigDecimal("45.00"),
                response.getPrice()
        );
        assertTrue(response.isAvailable());

        verify(userRepository).findById(userId);
        verify(bookRepository).findById(bookId);

        verify(wishlistItemRepository)
                .existsByUserIdAndBookId(
                        userId,
                        bookId
                );

        verify(wishlistItemRepository)
                .save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_shouldRejectDuplicateBook() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(wishlistItemRepository.existsByUserIdAndBookId(
                userId,
                bookId
        )).thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> wishlistService.addToWishlist(
                        userId,
                        bookId
                )
        );

        verify(userRepository).findById(userId);
        verify(bookRepository).findById(bookId);

        verify(wishlistItemRepository)
                .existsByUserIdAndBookId(
                        userId,
                        bookId
                );

        verify(wishlistItemRepository, never())
                .save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.addToWishlist(
                        userId,
                        bookId
                )
        );

        verify(userRepository).findById(userId);

        verify(bookRepository, never())
                .findById(any());

        verify(wishlistItemRepository, never())
                .save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_shouldThrowExceptionWhenBookDoesNotExist() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.addToWishlist(
                        userId,
                        bookId
                )
        );

        verify(userRepository).findById(userId);
        verify(bookRepository).findById(bookId);

        verify(wishlistItemRepository, never())
                .existsByUserIdAndBookId(
                        any(),
                        any()
                );

        verify(wishlistItemRepository, never())
                .save(any(WishlistItem.class));
    }

    // ============================================================
    // REMOVE FROM WISHLIST
    // ============================================================

    @Test
    void removeFromWishlist_shouldRemoveBookSuccessfully() {

        when(wishlistItemRepository.findByUserIdAndBookId(
                userId,
                bookId
        )).thenReturn(Optional.of(wishlistItem));

        wishlistService.removeFromWishlist(
                userId,
                bookId
        );

        verify(wishlistItemRepository)
                .findByUserIdAndBookId(
                        userId,
                        bookId
                );

        verify(wishlistItemRepository)
                .delete(wishlistItem);
    }

    @Test
    void removeFromWishlist_shouldThrowExceptionWhenBookNotInWishlist() {

        when(wishlistItemRepository.findByUserIdAndBookId(
                userId,
                bookId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> wishlistService.removeFromWishlist(
                        userId,
                        bookId
                )
        );

        verify(wishlistItemRepository)
                .findByUserIdAndBookId(
                        userId,
                        bookId
                );

        verify(wishlistItemRepository, never())
                .delete(any(WishlistItem.class));
    }

    // ============================================================
    // BOOK AVAILABILITY
    // ============================================================

    @Test
    void getWishlist_shouldMarkBookUnavailableWhenOutOfStock() {

        book.setStockQuantity(0);

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wishlistItem));

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        assertFalse(
                response.get(0).isAvailable()
        );
    }

    @Test
    void getWishlist_shouldMarkBookUnavailableWhenInactive() {

        book.setStatus(BookStatus.INACTIVE);

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wishlistItem));

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        assertFalse(
                response.get(0).isAvailable()
        );
    }
}