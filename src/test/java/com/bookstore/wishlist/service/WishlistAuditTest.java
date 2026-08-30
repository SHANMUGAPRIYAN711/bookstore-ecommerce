package com.bookstore.wishlist.service;

import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.wishlist.dto.WishlistItemResponse;
import com.bookstore.wishlist.entity.WishlistItem;
import com.bookstore.wishlist.repository.WishlistItemRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistAuditTest {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuditService auditService;

    private WishlistService wishlistService;

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

        WishlistServiceImpl target =
                new WishlistServiceImpl(
                        wishlistItemRepository,
                        userRepository,
                        bookRepository
                );

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        wishlistService = factory.getProxy();

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
                .imageUrl(
                        "https://example.com/clean-code.jpg"
                )
                .status(BookStatus.ACTIVE)
                .build();

        book.setId(bookId);

        wishlistItem = WishlistItem.builder()
                .user(user)
                .book(book)
                .build();

        wishlistItem.setId(wishlistItemId);

        configureSecurity();
    }

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // GET WISHLIST
    // ============================================================

    @Test
    void getWishlist_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "GET",
                "/api/wishlist"
        );

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wishlistItem));

        List<WishlistItemResponse> response =
                wishlistService.getWishlist(userId);

        assertEquals(
                1,
                response.size()
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "GET_WISHLIST",
                auditLog.getAction()
        );

        assertEquals(
                "WISHLIST",
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
                "/api/wishlist",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // ADD TO WISHLIST
    // ============================================================

    @Test
    void addToWishlist_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "POST",
                "/api/wishlist/books/" + bookId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(wishlistItemRepository
                .existsByUserIdAndBookId(
                        userId,
                        bookId
                ))
                .thenReturn(false);

        when(wishlistItemRepository.save(
                any(WishlistItem.class)
        )).thenReturn(wishlistItem);

        WishlistItemResponse response =
                wishlistService.addToWishlist(
                        userId,
                        bookId
                );

        assertEquals(
                bookId,
                response.getBookId()
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "ADD_TO_WISHLIST",
                auditLog.getAction()
        );

        assertEquals(
                "WISHLIST",
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
                "/api/wishlist/books/" + bookId,
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(auditLog.isSuccess());
    }

    // ============================================================
    // REMOVE FROM WISHLIST
    // ============================================================

    @Test
    void removeFromWishlist_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "DELETE",
                "/api/wishlist/books/" + bookId
        );

        when(wishlistItemRepository.findByUserIdAndBookId(
                userId,
                bookId
        )).thenReturn(Optional.of(wishlistItem));

        wishlistService.removeFromWishlist(
                userId,
                bookId
        );

        AuditLog auditLog = captureAuditLog();

        assertEquals(
                "REMOVE_FROM_WISHLIST",
                auditLog.getAction()
        );

        assertEquals(
                "WISHLIST",
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
                "/api/wishlist/books/" + bookId,
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
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