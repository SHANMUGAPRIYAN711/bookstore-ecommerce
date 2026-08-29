package com.bookstore.cart.service;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartItemRepository;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID userId;
    private UUID bookId;
    private UUID cartId;
    private UUID cartItemId;

    private User user;
    private Book book;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

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

        cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        cart.setId(cartId);

        cartItem = CartItem.builder()
                .cart(cart)
                .book(book)
                .quantity(2)
                .build();

        cartItem.setId(cartItemId);

        cart.getItems().add(cartItem);
    }

    // ============================================================
    // GET CART
    // ============================================================

    @Test
    void getCart_shouldReturnCartSuccessfully() {

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        CartResponse response =
                cartService.getCart(userId);

        assertNotNull(response);
        assertEquals(cartId, response.getId());

        assertEquals(1, response.getItems().size());
        assertEquals(bookId,
                response.getItems().get(0).getBookId());

        assertEquals(
                "Clean Code",
                response.getItems().get(0).getBookTitle()
        );

        assertEquals(
                2,
                response.getItems().get(0).getQuantity()
        );

        assertEquals(
                new BigDecimal("45.00"),
                response.getItems().get(0).getUnitPrice()
        );

        assertEquals(
                new BigDecimal("90.00"),
                response.getItems().get(0).getSubtotal()
        );

        assertEquals(2, response.getTotalItems());

        assertEquals(
                new BigDecimal("90.00"),
                response.getTotalAmount()
        );

        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void getCart_shouldCreateCartWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CartResponse response =
                cartService.getCart(userId);

        assertNotNull(response);
        assertNotNull(response.getItems());
        assertTrue(response.getItems().isEmpty());

        assertEquals(
                0,
                response.getTotalItems()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.getTotalAmount()
        );

        verify(cartRepository).findByUserId(userId);
        verify(userRepository).findById(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCart_shouldThrowExceptionWhenUserDoesNotExist() {

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart(userId)
        );

        verify(userRepository).findById(userId);
        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    // ============================================================
    // ADD TO CART
    // ============================================================

    @Test
    void addToCart_shouldAddNewBookSuccessfully() {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(2)
                        .build();

        Cart emptyCart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        emptyCart.setId(cartId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(emptyCart));

        when(cartItemRepository.findByCartIdAndBookId(
                cartId,
                bookId
        )).thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CartResponse response =
                cartService.addToCart(userId, request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());

        assertEquals(
                bookId,
                response.getItems().get(0).getBookId()
        );

        assertEquals(
                2,
                response.getItems().get(0).getQuantity()
        );

        assertEquals(
                new BigDecimal("90.00"),
                response.getTotalAmount()
        );

        verify(userRepository).findById(userId);
        verify(bookRepository).findById(bookId);

        verify(cartItemRepository)
                .findByCartIdAndBookId(cartId, bookId);

        verify(cartRepository).save(emptyCart);
    }

    @Test
    void addToCart_shouldIncreaseQuantityWhenBookAlreadyExists() {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(3)
                        .build();

        Cart existingCart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        existingCart.setId(cartId);

        CartItem existingItem = CartItem.builder()
                .cart(existingCart)
                .book(book)
                .quantity(2)
                .build();

        existingItem.setId(cartItemId);

        existingCart.getItems().add(existingItem);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingCart));

        when(cartItemRepository.findByCartIdAndBookId(
                cartId,
                bookId
        )).thenReturn(Optional.of(existingItem));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CartResponse response =
                cartService.addToCart(userId, request);

        assertEquals(
                5,
                existingItem.getQuantity()
        );

        assertEquals(
                5,
                response.getTotalItems()
        );

        assertEquals(
                new BigDecimal("225.00"),
                response.getTotalAmount()
        );

        verify(cartItemRepository)
                .findByCartIdAndBookId(cartId, bookId);

        verify(cartRepository).save(existingCart);
    }

    @Test
    void addToCart_shouldThrowExceptionWhenUserDoesNotExist() {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(1)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addToCart(
                        userId,
                        request
                )
        );

        verify(userRepository).findById(userId);
        verify(bookRepository, never())
                .findById(any());
    }

    @Test
    void addToCart_shouldThrowExceptionWhenBookDoesNotExist() {

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(1)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addToCart(
                        userId,
                        request
                )
        );

        verify(userRepository).findById(userId);
        verify(bookRepository).findById(bookId);
        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    // ============================================================
    // UPDATE CART ITEM
    // ============================================================

    @Test
    void updateCartItem_shouldUpdateQuantitySuccessfully() {

        UpdateCartItemRequest request =
                UpdateCartItemRequest.builder()
                        .quantity(5)
                        .build();

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.of(cartItem));

        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CartResponse response =
                cartService.updateCartItem(
                        userId,
                        cartItemId,
                        request
                );

        assertEquals(
                5,
                cartItem.getQuantity()
        );

        assertEquals(
                5,
                response.getTotalItems()
        );

        assertEquals(
                new BigDecimal("225.00"),
                response.getTotalAmount()
        );

        verify(cartItemRepository)
                .findByIdAndCartUserId(
                        cartItemId,
                        userId
                );

        verify(cartItemRepository)
                .save(cartItem);
    }

    @Test
    void updateCartItem_shouldThrowExceptionWhenItemDoesNotExist() {

        UpdateCartItemRequest request =
                UpdateCartItemRequest.builder()
                        .quantity(5)
                        .build();

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateCartItem(
                        userId,
                        cartItemId,
                        request
                )
        );

        verify(cartItemRepository)
                .findByIdAndCartUserId(
                        cartItemId,
                        userId
                );

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }

    // ============================================================
    // REMOVE CART ITEM
    // ============================================================

    @Test
    void removeCartItem_shouldRemoveItemSuccessfully() {

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.of(cartItem));

        cartService.removeCartItem(
                userId,
                cartItemId
        );

        assertTrue(cart.getItems().isEmpty());

        verify(cartItemRepository)
                .findByIdAndCartUserId(
                        cartItemId,
                        userId
                );
    }

    @Test
    void removeCartItem_shouldThrowExceptionWhenItemDoesNotExist() {

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeCartItem(
                        userId,
                        cartItemId
                )
        );

        verify(cartItemRepository)
                .findByIdAndCartUserId(
                        cartItemId,
                        userId
                );
    }

    // ============================================================
    // CLEAR CART
    // ============================================================

    @Test
    void clearCart_shouldRemoveAllItemsSuccessfully() {

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository)
                .findByUserId(userId);
    }

    @Test
    void clearCart_shouldThrowExceptionWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.clearCart(userId)
        );

        verify(cartRepository)
                .findByUserId(userId);
    }
}