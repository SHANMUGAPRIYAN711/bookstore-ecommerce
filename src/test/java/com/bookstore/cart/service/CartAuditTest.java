package com.bookstore.cart.service;

import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
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
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartAuditTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuditService auditService;

    private CartService cartService;

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

        CartServiceImpl target =
                new CartServiceImpl(
                        cartRepository,
                        cartItemRepository,
                        userRepository,
                        bookRepository
                );

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        cartService = factory.getProxy();

        user = User.builder()
                .firstName("Sauvik")
                .lastName("Nandi")
                .email("sauvik@bookstore.com")
                .password("encoded-password")
                .build();

        user.setId(userId);

        book = Book.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("Software craftsmanship")
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

        configureSecurity();
    }

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // GET CART
    // ============================================================

    @Test
    void getCart_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "GET",
                "/api/cart"
        );

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        CartResponse response =
                cartService.getCart(userId);

        assertEquals(
                cartId,
                response.getId()
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "GET_CART",
                auditLog.getAction()
        );

        assertEquals(
                "CART",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "GET",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/cart",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // ADD TO CART
    // ============================================================

    @Test
    void addToCart_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "POST",
                "/api/cart/items"
        );

        AddToCartRequest request =
                AddToCartRequest.builder()
                        .bookId(bookId)
                        .quantity(2)
                        .build();

        Cart emptyCart =
                Cart.builder()
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
                cartService.addToCart(
                        userId,
                        request
                );

        assertEquals(
                cartId,
                response.getId()
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "ADD_TO_CART",
                auditLog.getAction()
        );

        assertEquals(
                "CART",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/cart/items",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // UPDATE CART
    // ============================================================

    @Test
    void updateCartItem_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "PUT",
                "/api/cart/items/" + cartItemId
        );

        UpdateCartItemRequest request =
                UpdateCartItemRequest.builder()
                        .quantity(5)
                        .build();

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.of(cartItem));

        when(cartItemRepository.save(
                any(CartItem.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0));

        CartResponse response =
                cartService.updateCartItem(
                        userId,
                        cartItemId,
                        request
                );

        assertEquals(
                5,
                response.getTotalItems()
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "UPDATE_CART",
                auditLog.getAction()
        );

        assertEquals(
                "CART",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "PUT",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/cart/items/" + cartItemId,
                auditLog.getRequestUri()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // REMOVE CART ITEM
    // ============================================================

    @Test
    void removeCartItem_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "DELETE",
                "/api/cart/items/" + cartItemId
        );

        when(cartItemRepository.findByIdAndCartUserId(
                cartItemId,
                userId
        )).thenReturn(Optional.of(cartItem));

        cartService.removeCartItem(
                userId,
                cartItemId
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "REMOVE_FROM_CART",
                auditLog.getAction()
        );

        assertEquals(
                "CART",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "DELETE",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/cart/items/" + cartItemId,
                auditLog.getRequestUri()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // CLEAR CART
    // ============================================================

    @Test
    void clearCart_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "DELETE",
                "/api/cart"
        );

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "CLEAR_CART",
                auditLog.getAction()
        );

        assertEquals(
                "CART",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "DELETE",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/cart",
                auditLog.getRequestUri()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private AuditLog captureAuditLog() {

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        return captor.getValue();
    }

    private void configureSecurity() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "sauvik@bookstore.com",
                        null,
                        Collections.emptyList()
                );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                securityContext
        );
    }

    private void configureRequest(
            String method,
            String uri
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod(method);
        request.setRequestURI(uri);
        request.setRemoteAddr(
                "192.168.1.10"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }
}