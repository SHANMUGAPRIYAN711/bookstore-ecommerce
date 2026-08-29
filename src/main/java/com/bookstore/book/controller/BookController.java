package com.bookstore.book.controller;

import com.bookstore.book.dto.BookCreateRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.BookSearchRequest;
import com.bookstore.book.dto.BookUpdateRequest;
import com.bookstore.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Creates a new book.
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request) {

        BookResponse response = bookService.createBook(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves a book by its UUID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                bookService.getBookById(id)
        );
    }

    /**
     * Retrieves all books with pagination, sorting
     * and optional filtering.
     */
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        return ResponseEntity.ok(
                bookService.getAllBooks(
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /**
     * Searches books using the supplied search criteria.
     */
    @PostMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @Valid @RequestBody BookSearchRequest request) {

        return ResponseEntity.ok(
                bookService.searchBooks(request)
        );
    }

    /**
     * Retrieves active books.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<BookResponse>> getActiveBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        return ResponseEntity.ok(
                bookService.getActiveBooks(
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /**
     * Retrieves books that are currently available
     * for purchase.
     */
    @GetMapping("/available")
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        return ResponseEntity.ok(
                bookService.getAvailableBooks(
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /**
     * Updates an existing book.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookUpdateRequest request) {

        return ResponseEntity.ok(
                bookService.updateBook(id, request)
        );
    }

    /**
     * Deletes a book.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID id) {

        bookService.deleteBook(id);

        return ResponseEntity.noContent().build();
    }
}