package com.bookstore.inventory.service;

import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.exception.BadRequestException;
import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class InventoryAuditTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuditService auditService;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {

        InventoryServiceImpl target =
                new InventoryServiceImpl(bookRepository);

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        inventoryService =
                factory.getProxy();

        configureSecurityAndRequest();
    }

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void increaseStock_shouldCreateSuccessfulAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Spring Boot Guide")
                        .stockQuantity(10)
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.increaseStock(
                        bookId,
                        5
                );

        assertEquals(
                15,
                response.getStockQuantity()
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        AuditLog auditLog =
                captor.getValue();

        assertEquals(
                "INCREASE_STOCK",
                auditLog.getAction()
        );

        assertEquals(
                "INVENTORY",
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
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    @Test
    void decreaseStock_shouldCreateSuccessfulAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Spring Boot Guide")
                        .stockQuantity(10)
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.decreaseStock(
                        bookId,
                        3
                );

        assertEquals(
                7,
                response.getStockQuantity()
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        AuditLog auditLog =
                captor.getValue();

        assertEquals(
                "DECREASE_STOCK",
                auditLog.getAction()
        );

        assertEquals(
                "INVENTORY",
                auditLog.getEntity()
        );

        assertEquals(
                "admin@bookstore.com",
                auditLog.getUsername()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    @Test
    void updateStock_shouldCreateSuccessfulAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Spring Boot Guide")
                        .stockQuantity(10)
                        .build();

        UpdateInventoryRequest request =
                new UpdateInventoryRequest();

        request.setStockQuantity(25);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.updateStock(
                        bookId,
                        request
                );

        assertEquals(
                25,
                response.getStockQuantity()
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        AuditLog auditLog =
                captor.getValue();

        assertEquals(
                "UPDATE_STOCK",
                auditLog.getAction()
        );

        assertEquals(
                "INVENTORY",
                auditLog.getEntity()
        );

        assertEquals(
                "admin@bookstore.com",
                auditLog.getUsername()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    @Test
    void decreaseStock_whenInsufficientStock_shouldCreateFailureAuditLog() {

        UUID bookId =
                UUID.randomUUID();

        Book book =
                Book.builder()
                        .title("Spring Boot Guide")
                        .stockQuantity(5)
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        try {

            inventoryService.decreaseStock(
                    bookId,
                    10
            );

        } catch (BadRequestException exception) {

            ArgumentCaptor<AuditLog> captor =
                    ArgumentCaptor.forClass(AuditLog.class);

            verify(auditService)
                    .save(captor.capture());

            AuditLog auditLog =
                    captor.getValue();

            assertEquals(
                    "DECREASE_STOCK",
                    auditLog.getAction()
            );

            assertEquals(
                    "INVENTORY",
                    auditLog.getEntity()
            );

            assertEquals(
                    "admin@bookstore.com",
                    auditLog.getUsername()
            );

            assertFalse(
                    auditLog.isSuccess()
            );

            assertEquals(
                    "BadRequestException",
                    auditLog.getExceptionType()
            );

            assertEquals(
                    "Insufficient stock. Available stock: 5",
                    auditLog.getErrorMessage()
            );

            return;
        }

        throw new AssertionError(
                "Expected BadRequestException"
        );
    }

    private void configureSecurityAndRequest() {

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin@bookstore.com",
                                null,
                                List.of()
                        )
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");

        request.setRequestURI(
                "/api/inventory"
        );

        request.setRemoteAddr(
                "192.168.1.10"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }
}