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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private UUID bookId;
    private Book book;

    @BeforeEach
    void setUp() {

        bookId = UUID.randomUUID();

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
    }

    // ============================================================
    // CREATE BOOK
    // ============================================================

    @Test
    void createBook_shouldCreateBookSuccessfully() {

        BookCreateRequest request = BookCreateRequest.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("A handbook of agile software craftsmanship")
                .category("Programming")
                .price(new BigDecimal("45.00"))
                .stockQuantity(10)
                .imageUrl("https://example.com/clean-code.jpg")
                .build();

        when(bookRepository.existsByIsbn(request.getIsbn()))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        BookResponse response = bookService.createBook(request);

        assertNotNull(response);
        assertEquals(bookId, response.getId());
        assertEquals("Clean Code", response.getTitle());
        assertEquals("9780132350884", response.getIsbn());
        assertEquals("Robert Martin", response.getAuthor());
        assertEquals(new BigDecimal("45.00"), response.getPrice());
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.isActive());

        verify(bookRepository).existsByIsbn("9780132350884");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_shouldThrowExceptionWhenIsbnAlreadyExists() {

        BookCreateRequest request = BookCreateRequest.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .category("Programming")
                .price(new BigDecimal("45.00"))
                .stockQuantity(10)
                .build();

        when(bookRepository.existsByIsbn(request.getIsbn()))
                .thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.createBook(request)
        );

        assertTrue(exception.getMessage().contains("ISBN"));

        verify(bookRepository).existsByIsbn("9780132350884");
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ============================================================
    // GET BOOK BY ID
    // ============================================================

    @Test
    void getBookById_shouldReturnBookSuccessfully() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(bookId);

        assertNotNull(response);
        assertEquals(bookId, response.getId());
        assertEquals("Clean Code", response.getTitle());
        assertEquals("Programming", response.getCategory());
        assertEquals(10, response.getStockQuantity());

        verify(bookRepository).findById(bookId);
    }

    @Test
    void getBookById_shouldThrowExceptionWhenBookDoesNotExist() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBookById(bookId)
        );

        assertTrue(exception.getMessage().contains("Book not found"));

        verify(bookRepository).findById(bookId);
    }

    @Test
    void getBookById_shouldThrowExceptionWhenIdIsNull() {

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.getBookById(null)
        );

        assertEquals("Book ID is required", exception.getMessage());

        verify(bookRepository, never()).findById(any());
    }

    // ============================================================
    // GET ALL BOOKS
    // ============================================================

    @Test
    void getAllBooks_shouldReturnPaginatedBooks() {

        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<BookResponse> response =
                bookService.getAllBooks(
                        0,
                        10,
                        "title",
                        "ASC"
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Clean Code", response.getContent().get(0).getTitle());

        verify(bookRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllBooks_shouldRejectNegativePage() {

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.getAllBooks(
                        -1,
                        10,
                        "title",
                        "ASC"
                )
        );

        assertEquals("Page cannot be negative", exception.getMessage());

        verify(bookRepository, never())
                .findAll(any(Pageable.class));
    }

    @Test
    void getAllBooks_shouldRejectInvalidPageSize() {

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.getAllBooks(
                        0,
                        101,
                        "title",
                        "ASC"
                )
        );

        assertEquals(
                "Page size must be between 1 and 100",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .findAll(any(Pageable.class));
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    void searchBooks_shouldReturnMatchingBooks() {

        BookSearchRequest request = BookSearchRequest.builder()
                .keyword("clean")
                .category("Programming")
                .minPrice(new BigDecimal("20.00"))
                .maxPrice(new BigDecimal("100.00"))
                .page(0)
                .size(20)
                .sortBy("title")
                .sortDirection("ASC")
                .build();

        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.searchBooks(
                eq("clean"),
                eq("Programming"),
                eq(new BigDecimal("20.00")),
                eq(new BigDecimal("100.00")),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookResponse> response =
                bookService.searchBooks(request);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Clean Code",
                response.getContent().get(0).getTitle()
        );

        verify(bookRepository).searchBooks(
                eq("clean"),
                eq("Programming"),
                eq(new BigDecimal("20.00")),
                eq(new BigDecimal("100.00")),
                any(Pageable.class)
        );
    }

    @Test
    void searchBooks_shouldRejectInvalidPriceRange() {

        BookSearchRequest request = BookSearchRequest.builder()
                .minPrice(new BigDecimal("100.00"))
                .maxPrice(new BigDecimal("50.00"))
                .build();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.searchBooks(request)
        );

        assertEquals(
                "Minimum price cannot be greater than maximum price",
                exception.getMessage()
        );

        verify(bookRepository, never()).searchBooks(
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        );
    }

    @Test
    void searchBooks_shouldRejectInvalidRating() {

        BookSearchRequest request = BookSearchRequest.builder()
                .minRating(6.0)
                .build();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.searchBooks(request)
        );

        assertEquals(
                "Rating must be between 0 and 5",
                exception.getMessage()
        );

        verify(bookRepository, never()).searchBooks(
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        );
    }

    @Test
    void searchBooks_shouldRejectInvalidSortField() {

        BookSearchRequest request = BookSearchRequest.builder()
                .sortBy("password")
                .sortDirection("ASC")
                .build();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.searchBooks(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid sort field")
        );

        verify(bookRepository, never()).searchBooks(
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        );
    }

    // ============================================================
    // ACTIVE BOOKS
    // ============================================================

    @Test
    void getActiveBooks_shouldReturnActiveBooks() {

        Page<Book> page = new PageImpl<>(List.of(book));

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
        assertEquals(1, response.getTotalElements());
        assertTrue(response.getContent().get(0).isActive());

        verify(bookRepository).findByStatus(
                eq(BookStatus.ACTIVE),
                any(Pageable.class)
        );
    }

    // ============================================================
    // AVAILABLE BOOKS
    // ============================================================

    @Test
    void getAvailableBooks_shouldReturnBooksWithStock() {

        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAvailableBooks(
                eq(BookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookResponse> response =
                bookService.getAvailableBooks(
                        0,
                        10,
                        "price",
                        "ASC"
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(10,
                response.getContent()
                        .get(0)
                        .getStockQuantity());

        verify(bookRepository).findAvailableBooks(
                eq(BookStatus.ACTIVE),
                any(Pageable.class)
        );
    }

    // ============================================================
    // UPDATE BOOK
    // ============================================================

    @Test
    void updateBook_shouldUpdateBookSuccessfully() {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .title("Clean Code Updated")
                .author("Robert C. Martin")
                .category("Software Engineering")
                .price(new BigDecimal("50.00"))
                .imageUrl("https://example.com/new-image.jpg")
                .active(true)
                .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response =
                bookService.updateBook(bookId, request);

        assertNotNull(response);
        assertEquals(
                "Clean Code Updated",
                response.getTitle()
        );
        assertEquals(
                "Robert C. Martin",
                response.getAuthor()
        );
        assertEquals(
                "Software Engineering",
                response.getCategory()
        );
        assertEquals(
                new BigDecimal("50.00"),
                response.getPrice()
        );
        assertTrue(response.isActive());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_shouldRejectDuplicateIsbn() {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .isbn("9780000000000")
                .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.existsByIsbn("9780000000000"))
                .thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertTrue(exception.getMessage().contains("ISBN"));

        verify(bookRepository).existsByIsbn("9780000000000");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_shouldRejectNegativePrice() {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .price(new BigDecimal("-10.00"))
                .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertEquals(
                "Price must be greater than zero",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void updateBook_shouldRejectNegativeStock() {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .stockQuantity(-5)
                .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertEquals(
                "Stock quantity cannot be negative",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    // ============================================================
    // DELETE BOOK
    // ============================================================

    @Test
    void deleteBook_shouldSoftDeleteBook() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        bookService.deleteBook(bookId);

        assertEquals(
                BookStatus.INACTIVE,
                book.getStatus()
        );

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteBook_shouldThrowExceptionWhenBookDoesNotExist() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.deleteBook(bookId)
        );

        assertTrue(
                exception.getMessage()
                        .contains("Book not found")
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }
}