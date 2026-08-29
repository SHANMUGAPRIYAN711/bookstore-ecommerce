package com.bookstore.book.service;

import com.bookstore.book.dto.BookCreateRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.BookSearchRequest;
import com.bookstore.book.dto.BookUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BookService {

    /**
     * Creates a new book in the catalog.
     */
    BookResponse createBook(BookCreateRequest request);

    /**
     * Retrieves a book by its identifier.
     */
    BookResponse getBookById(UUID id);

    /**
     * Retrieves all books with pagination and sorting.
     */
    Page<BookResponse> getAllBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    /**
     * Searches and filters books.
     */
    Page<BookResponse> searchBooks(
            BookSearchRequest request
    );

    /**
     * Retrieves active books.
     */
    Page<BookResponse> getActiveBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    /**
     * Retrieves books that are currently available for purchase.
     */
    Page<BookResponse> getAvailableBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    /**
     * Updates an existing book.
     */
    BookResponse updateBook(
            UUID id,
            BookUpdateRequest request
    );

    /**
     * Deletes a book from the catalog.
     *
     * <p>
     * The implementation performs a soft delete by changing the
     * book status to INACTIVE.
     * </p>
     */
    void deleteBook(UUID id);
}