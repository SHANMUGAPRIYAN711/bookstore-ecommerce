package com.bookstore.cart.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartItemResponse;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartItemRepository;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Auditable(
            action = "GET_CART",
            entity = "CART"
    )
    @Override
    public CartResponse getCart(UUID userId) {

        Cart cart = getOrCreateCart(userId);

        return mapToCartResponse(cart);
    }

    @Auditable(
            action = "ADD_TO_CART",
            entity = "CART"
    )
    @Override
    public CartResponse addToCart(
            UUID userId,
            AddToCartRequest request
    ) {

        User user = getUser(userId);

        Book book = getBook(request.getBookId());

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(user));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndBookId(
                        cart.getId(),
                        book.getId()
                )
                .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(
                    cartItem.getQuantity()
                            + request.getQuantity()
            );

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(request.getQuantity())
                    .build();

            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Auditable(
            action = "UPDATE_CART",
            entity = "CART"
    )
    @Override
    public CartResponse updateCartItem(
            UUID userId,
            UUID itemId,
            UpdateCartItemRequest request
    ) {

        CartItem cartItem = cartItemRepository
                .findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found"
                        )
                );

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return mapToCartResponse(
                cartItem.getCart()
        );
    }

    @Auditable(
            action = "REMOVE_FROM_CART",
            entity = "CART"
    )
    @Override
    public void removeCartItem(
            UUID userId,
            UUID itemId
    ) {

        CartItem cartItem = cartItemRepository
                .findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found"
                        )
                );

        Cart cart = cartItem.getCart();

        cart.getItems().remove(cartItem);

        cartItemRepository.delete(cartItem);
    }

    @Auditable(
            action = "CLEAR_CART",
            entity = "CART"
    )
    @Override
    public void clearCart(UUID userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        )
                );

        cart.getItems().clear();
    }

    private User getUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Book getBook(UUID bookId) {

        return bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );
    }

    private Cart createCart(User user) {

        Cart cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        return cartRepository.save(cart);
    }

    private Cart getOrCreateCart(UUID userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = getUser(userId);
                    return createCart(user);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {

        List<CartItemResponse> itemResponses =
                cart.getItems()
                        .stream()
                        .map(this::mapToCartItemResponse)
                        .toList();

        int totalItems = cart.getItems()
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount =
                cart.getItems()
                        .stream()
                        .map(item ->
                                item.getBook()
                                        .getPrice()
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        item.getQuantity()
                                                )
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(
            CartItem cartItem
    ) {

        Book book = cartItem.getBook();

        BigDecimal unitPrice = book.getPrice();

        BigDecimal subtotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(
                                cartItem.getQuantity()
                        )
                );

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .imageUrl(book.getImageUrl())
                .unitPrice(unitPrice)
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}