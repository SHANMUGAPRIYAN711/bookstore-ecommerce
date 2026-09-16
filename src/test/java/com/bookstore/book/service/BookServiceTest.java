package com.bookstore.book.service;

import com.bookstore.book.dto.BookCreateRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.BookSearchRequest;
import com.bookstore.book.dto.BookUpdateRequest;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.storage.service.StorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private BookCacheService bookCacheService;

    @InjectMocks
    private BookServiceImpl bookService;

    private UUID bookId;

    private Book book;

    // ============================================================
    // SETUP
    // ============================================================

    @BeforeEach
    void setUp() {

        bookId = UUID.randomUUID();

        book = Book.builder()
                .title("Spring Boot Guide")
                .isbn("9781234567890")
                .author("John Doe")
                .description("Spring Boot learning guide")
                .category("Technology")
                .price(new BigDecimal("599.99"))
                .stockQuantity(10)
                .imageKey("uploads/book.jpg")
                .status(BookStatus.ACTIVE)
                .build();

        book.setId(bookId);

        /*
         * BookServiceImpl generates a presigned URL while mapping
         * the Book entity into BookResponse.
         *
         * Some tests do not reach the response mapping logic.
         * Therefore lenient() prevents Mockito from reporting this
         * shared stubbing as unnecessary.
         */
        lenient()
                .when(storageService.generatePresignedUrl(anyString()))
                .thenReturn("https://test-presigned-url");
    }

    // ============================================================
    // CREATE BOOK
    // ============================================================

    @Test
    void createBook_shouldCreateBookSuccessfully() {

        BookCreateRequest request =
                BookCreateRequest.builder()
                        .title("Spring Boot Guide")
                        .isbn("9781234567890")
                        .author("John Doe")
                        .description("Spring Boot learning guide")
                        .category("Technology")
                        .price(new BigDecimal("599.99"))
                        .stockQuantity(10)
                        .imageKey("uploads/book.jpg")
                        .build();

        when(bookRepository.existsByIsbn(request.getIsbn()))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        BookResponse response =
                bookService.createBook(request);

        assertNotNull(response);

        assertEquals(
                "Spring Boot Guide",
                response.getTitle()
        );

        assertEquals(
                "9781234567890",
                response.getIsbn()
        );

        assertEquals(
                "John Doe",
                response.getAuthor()
        );

        assertEquals(
                "Technology",
                response.getCategory()
        );

        assertEquals(
                new BigDecimal("599.99"),
                response.getPrice()
        );

        assertEquals(
                10,
                response.getStockQuantity()
        );

        verify(bookRepository)
                .existsByIsbn("9781234567890");

        verify(bookRepository)
                .save(any(Book.class));
    }

    @Test
    void createBook_whenIsbnAlreadyExists_shouldThrowException() {

        BookCreateRequest request =
                BookCreateRequest.builder()
                        .title("Spring Boot Guide")
                        .isbn("9781234567890")
                        .author("John Doe")
                        .description("Spring Boot learning guide")
                        .category("Technology")
                        .price(new BigDecimal("599.99"))
                        .stockQuantity(10)
                        .build();

        when(bookRepository.existsByIsbn(request.getIsbn()))
                .thenReturn(true);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> bookService.createBook(request)
                );

        assertEquals(
                "A book with ISBN already exists: 9781234567890",
                exception.getMessage()
        );

        verify(bookRepository)
                .existsByIsbn("9781234567890");

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    // ============================================================
    // GET BOOK BY ID
    // ============================================================

    @Test
    void getBookById_shouldReturnBookSuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BookResponse response =
                bookService.getBookById(bookId);

        assertNotNull(response);

        assertEquals(
                "Spring Boot Guide",
                response.getTitle()
        );

        assertEquals(
                "9781234567890",
                response.getIsbn()
        );

        assertEquals(
                "John Doe",
                response.getAuthor()
        );

        assertEquals(
                "Technology",
                response.getCategory()
        );

        assertEquals(
                new BigDecimal("599.99"),
                response.getPrice()
        );

        assertEquals(
                10,
                response.getStockQuantity()
        );

        verify(bookRepository)
                .findById(bookId);
    }

    @Test
    void getBookById_whenBookDoesNotExist_shouldThrowException() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> bookService.getBookById(bookId)
                );

        assertEquals(
                "Book not found with ID: " + bookId,
                exception.getMessage()
        );

        verify(bookRepository)
                .findById(bookId);
    }

    @Test
    void getBookById_whenBookIdIsNull_shouldThrowException() {

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> bookService.getBookById(null)
                );

        assertEquals(
                "Book ID is required",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .findById(any());
    }

    // ============================================================
    // GET ALL BOOKS
    // ============================================================

    @Test
    void getAllBooks_shouldReturnPaginatedBooks() {

        Page<Book> page =
                new PageImpl<>(
                        List.of(book)
                );

        when(bookRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<BookResponse> response =
                bookService.getAllBooks(
                        0,
                        10,
                        "createdAt",
                        "DESC"
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                1,
                response.getContent().size()
        );

        assertEquals(
                "Spring Boot Guide",
                response.getContent()
                        .get(0)
                        .getTitle()
        );

        verify(bookRepository)
                .findAll(any(Pageable.class));
    }

    // ============================================================
    // SEARCH BOOKS
    // ============================================================

    @Test
    void searchBooks_shouldReturnMatchingBooks() {

        BookSearchRequest request =
                BookSearchRequest.builder()
                        .keyword("Spring")
                        .category("Technology")
                        .minPrice(new BigDecimal("100"))
                        .maxPrice(new BigDecimal("1000"))
                        .page(0)
                        .size(20)
                        .sortBy("createdAt")
                        .sortDirection("DESC")
                        .build();

        Page<Book> page =
                new PageImpl<>(
                        List.of(book)
                );

        when(bookRepository.searchBooks(
                eq("Spring"),
                eq("Technology"),
                eq(new BigDecimal("100")),
                eq(new BigDecimal("1000")),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookResponse> response =
                bookService.searchBooks(request);

        assertNotNull(response);

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "Spring Boot Guide",
                response.getContent()
                        .get(0)
                        .getTitle()
        );

        verify(bookRepository)
                .searchBooks(
                        eq("Spring"),
                        eq("Technology"),
                        eq(new BigDecimal("100")),
                        eq(new BigDecimal("1000")),
                        any(Pageable.class)
                );
    }

    // ============================================================
    // ACTIVE BOOKS
    // ============================================================

    @Test
    void getActiveBooks_shouldReturnActiveBooks() {

        Page<Book> page =
                new PageImpl<>(
                        List.of(book)
                );

        when(bookRepository.findByStatus(
                eq(BookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookResponse> response =
                bookService.getActiveBooks(
                        0,
                        10,
                        "createdAt",
                        "DESC"
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "Spring Boot Guide",
                response.getContent()
                        .get(0)
                        .getTitle()
        );

        verify(bookRepository)
                .findByStatus(
                        eq(BookStatus.ACTIVE),
                        any(Pageable.class)
                );
    }

    // ============================================================
    // AVAILABLE BOOKS
    // ============================================================

    @Test
    void getAvailableBooks_shouldReturnBooksWithStock() {

        Page<Book> page =
                new PageImpl<>(
                        List.of(book)
                );

        when(bookRepository.findAvailableBooks(
                eq(BookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookResponse> response =
                bookService.getAvailableBooks(
                        0,
                        10,
                        "createdAt",
                        "DESC"
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                10,
                response.getContent()
                        .get(0)
                        .getStockQuantity()
        );

        assertTrue(
                response.getContent()
                        .get(0)
                        .isActive()
        );

        verify(bookRepository)
                .findAvailableBooks(
                        eq(BookStatus.ACTIVE),
                        any(Pageable.class)
                );
    }

    // ============================================================
    // UPDATE BOOK
    // ============================================================

    @Test
    void updateBook_shouldUpdateBookSuccessfully() {

        BookUpdateRequest request =
                BookUpdateRequest.builder()
                        .title("Advanced Spring Boot")
                        .author("Jane Doe")
                        .category("Programming")
                        .price(new BigDecimal("799.99"))
                        .stockQuantity(25)
                        .imageKey("uploads/new-book.jpg")
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

        assertNotNull(response);

        assertEquals(
                "Advanced Spring Boot",
                response.getTitle()
        );

        assertEquals(
                "Jane Doe",
                response.getAuthor()
        );

        assertEquals(
                "Programming",
                response.getCategory()
        );

        assertEquals(
                new BigDecimal("799.99"),
                response.getPrice()
        );

        assertEquals(
                25,
                response.getStockQuantity()
        );

        verify(bookRepository)
                .findById(bookId);

        verify(bookRepository)
                .save(any(Book.class));
    }

    @Test
    void updateBook_whenBookDoesNotExist_shouldThrowException() {

        BookUpdateRequest request =
                BookUpdateRequest.builder()
                        .title("Updated Book")
                        .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> bookService.updateBook(
                                bookId,
                                request
                        )
                );

        assertEquals(
                "Book not found with ID: " + bookId,
                exception.getMessage()
        );

        verify(bookRepository)
                .findById(bookId);

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    // ============================================================
    // DELETE BOOK
    // ============================================================

    @Test
    void deleteBook_shouldDeleteBookSuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        bookService.deleteBook(bookId);

        verify(bookRepository)
                .findById(bookId);

        verify(bookRepository)
                .save(book);

        assertEquals(
                BookStatus.INACTIVE,
                book.getStatus()
        );
    }

    @Test
    void deleteBook_whenBookDoesNotExist_shouldThrowException() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> bookService.deleteBook(bookId)
                );

        assertEquals(
                "Book not found with ID: " + bookId,
                exception.getMessage()
        );

        verify(bookRepository)
                .findById(bookId);

        verify(bookRepository, never())
                .delete(any(Book.class));
    }

    // ============================================================
    // RESPONSE MAPPING
    // ============================================================

    @Test
    void getBookById_shouldMapActiveStatusCorrectly() {

        book.setStatus(BookStatus.ACTIVE);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BookResponse response =
                bookService.getBookById(bookId);

        assertTrue(
                response.isActive()
        );
    }

    @Test
    void getBookById_shouldMapInactiveStatusCorrectly() {

        book.setStatus(BookStatus.INACTIVE);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BookResponse response =
                bookService.getBookById(bookId);

        assertFalse(
                response.isActive()
        );
    }
}