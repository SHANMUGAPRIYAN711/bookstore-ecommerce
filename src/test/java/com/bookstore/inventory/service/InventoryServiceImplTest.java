package com.bookstore.inventory.service;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();

        book = Book.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert C. Martin")
                .category("Programming")
                .stockQuantity(10)
                .build();

        book.setId(bookId);
    }

    @Test
    void getInventory_shouldReturnInventorySuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        InventoryResponse response =
                inventoryService.getInventory(bookId);

        assertNotNull(response);
        assertEquals(bookId, response.getBookId());
        assertEquals("Clean Code", response.getBookTitle());
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.isAvailable());

        verify(bookRepository).findById(bookId);
    }

    @Test
    void getInventory_shouldThrowExceptionWhenBookDoesNotExist() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.getInventory(bookId)
        );

        verify(bookRepository).findById(bookId);
    }

    @Test
    void updateStock_shouldUpdateStockSuccessfully() {

        UpdateInventoryRequest request =
                UpdateInventoryRequest.builder()
                        .stockQuantity(25)
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(book))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.updateStock(bookId, request);

        assertEquals(25, book.getStockQuantity());
        assertEquals(25, response.getStockQuantity());
        assertTrue(response.isAvailable());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void updateStock_shouldRejectNullRequest() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.updateStock(bookId, null)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void updateStock_shouldRejectNullStockQuantity() {

        UpdateInventoryRequest request =
                UpdateInventoryRequest.builder()
                        .stockQuantity(null)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.updateStock(bookId, request)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void updateStock_shouldRejectNegativeStock() {

        UpdateInventoryRequest request =
                UpdateInventoryRequest.builder()
                        .stockQuantity(-5)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.updateStock(bookId, request)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void increaseStock_shouldIncreaseStockSuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(book))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.increaseStock(bookId, 5);

        assertEquals(15, book.getStockQuantity());
        assertEquals(15, response.getStockQuantity());
        assertTrue(response.isAvailable());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void increaseStock_shouldRejectNegativeQuantity() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.increaseStock(bookId, -5)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void increaseStock_shouldRejectNullQuantity() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.increaseStock(bookId, null)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void decreaseStock_shouldDecreaseStockSuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(book))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.decreaseStock(bookId, 4);

        assertEquals(6, book.getStockQuantity());
        assertEquals(6, response.getStockQuantity());
        assertTrue(response.isAvailable());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void decreaseStock_shouldRejectWhenStockIsInsufficient() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.decreaseStock(bookId, 15)
        );

        assertEquals(10, book.getStockQuantity());

        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void decreaseStock_shouldRejectNegativeQuantity() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.decreaseStock(bookId, -3)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void decreaseStock_shouldAllowStockToReachZero() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(book))
                .thenReturn(book);

        InventoryResponse response =
                inventoryService.decreaseStock(bookId, 10);

        assertEquals(0, book.getStockQuantity());
        assertEquals(0, response.getStockQuantity());
        assertFalse(response.isAvailable());

        verify(bookRepository).save(book);
    }

    @Test
    void hasSufficientStock_shouldReturnTrueWhenStockIsSufficient() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        boolean result =
                inventoryService.hasSufficientStock(bookId, 5);

        assertTrue(result);

        verify(bookRepository).findById(bookId);
    }

    @Test
    void hasSufficientStock_shouldReturnFalseWhenStockIsInsufficient() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        boolean result =
                inventoryService.hasSufficientStock(bookId, 15);

        assertFalse(result);

        verify(bookRepository).findById(bookId);
    }

    @Test
    void hasSufficientStock_shouldReturnTrueWhenRequestedQuantityEqualsStock() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        boolean result =
                inventoryService.hasSufficientStock(bookId, 10);

        assertTrue(result);
    }

    @Test
    void hasSufficientStock_shouldRejectNegativeQuantity() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.hasSufficientStock(bookId, -1)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void isAvailable_shouldReturnTrueWhenStockIsGreaterThanZero() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        boolean result =
                inventoryService.isAvailable(bookId);

        assertTrue(result);

        verify(bookRepository).findById(bookId);
    }

    @Test
    void isAvailable_shouldReturnFalseWhenStockIsZero() {

        book.setStockQuantity(0);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        boolean result =
                inventoryService.isAvailable(bookId);

        assertFalse(result);

        verify(bookRepository).findById(bookId);
    }

    @Test
    void getInventory_shouldRejectNullBookId() {

        assertThrows(
                BadRequestException.class,
                () -> inventoryService.getInventory(null)
        );

        verifyNoInteractions(bookRepository);
    }
}