package com.bookstore.book.service;

import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import com.bookstore.book.dto.BookCreateRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.BookUpdateRequest;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.common.enums.BookStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookAuditTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuditService auditService;

    private BookService bookService;

    @BeforeEach
    void setUp() {

        BookServiceImpl target =
                new BookServiceImpl(bookRepository);

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        bookService =
                factory.getProxy();

        configureSecurityAndRequest();
    }

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createBook_shouldCreateSuccessfulAuditLog() {

        BookCreateRequest request =
                BookCreateRequest.builder()
                        .title("Spring Boot Guide")
                        .isbn("9781234567890")
                        .author("John Smith")
                        .description("Spring Boot book")
                        .category("Programming")
                        .price(new BigDecimal("799.00"))
                        .stockQuantity(10)
                        .imageUrl("https://example.com/book.jpg")
                        .build();

        Book savedBook =
                Book.builder()
                        .title("Spring Boot Guide")
                        .isbn("9781234567890")
                        .author("John Smith")
                        .description("Spring Boot book")
                        .category("Programming")
                        .price(new BigDecimal("799.00"))
                        .stockQuantity(10)
                        .imageUrl("https://example.com/book.jpg")
                        .status(BookStatus.ACTIVE)
                        .build();

        when(bookRepository.existsByIsbn("9781234567890"))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenReturn(savedBook);

        BookResponse response =
                bookService.createBook(request);

        assertEquals(
                "Spring Boot Guide",
                response.getTitle()
        );

        assertEquals(
                "9781234567890",
                response.getIsbn()
        );

        assertEquals(
                10,
                response.getStockQuantity()
        );

        assertTrue(
                response.isActive()
        );

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "CREATE_BOOK",
                auditLog.getAction()
        );

        assertEquals(
                "BOOK",
                auditLog.getEntity()
        );

        assertEquals(
                "admin@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/books",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    @Test
    void updateBook_shouldCreateSuccessfulAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Old Title")
                        .isbn("9781234567890")
                        .author("John Smith")
                        .description("Old description")
                        .category("Programming")
                        .price(new BigDecimal("500.00"))
                        .stockQuantity(10)
                        .imageUrl("old-image.jpg")
                        .status(BookStatus.ACTIVE)
                        .build();

        BookUpdateRequest request =
                BookUpdateRequest.builder()
                        .title("Updated Spring Boot Guide")
                        .price(new BigDecimal("799.00"))
                        .category("Java")
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        BookResponse response =
                bookService.updateBook(
                        bookId,
                        request
                );

        assertEquals(
                "Updated Spring Boot Guide",
                response.getTitle()
        );

        assertEquals(
                new BigDecimal("799.00"),
                response.getPrice()
        );

        assertEquals(
                "Java",
                response.getCategory()
        );

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "UPDATE_BOOK",
                auditLog.getAction()
        );

        assertEquals(
                "BOOK",
                auditLog.getEntity()
        );

        assertEquals(
                "admin@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/books",
                auditLog.getRequestUri()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    @Test
    void deleteBook_shouldCreateSuccessfulAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Spring Boot Guide")
                        .isbn("9781234567890")
                        .author("John Smith")
                        .description("Spring Boot book")
                        .category("Programming")
                        .price(new BigDecimal("799.00"))
                        .stockQuantity(10)
                        .status(BookStatus.ACTIVE)
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        bookService.deleteBook(bookId);

        assertEquals(
                BookStatus.INACTIVE,
                book.getStatus()
        );

        verify(bookRepository)
                .save(book);

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "DELETE_BOOK",
                auditLog.getAction()
        );

        assertEquals(
                "BOOK",
                auditLog.getEntity()
        );

        assertEquals(
                "admin@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/books",
                auditLog.getRequestUri()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    /**
     * Captures the AuditLog passed to AuditService.
     */
    private AuditLog captureAuditLog() {

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        return captor.getValue();
    }

    /**
     * Configures an authenticated security context
     * and a mock HTTP request for the audit tests.
     */
    private void configureSecurityAndRequest() {

        /*
         * The three-argument constructor creates an
         * authenticated UsernamePasswordAuthenticationToken.
         */
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@bookstore.com",
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

        /*
         * Configure mock HTTP request information.
         */
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");

        request.setRequestURI(
                "/api/books"
        );

        request.setRemoteAddr(
                "192.168.1.10"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }
}