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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    /**
     * Creates a new book.
     */
    @Override
    public BookResponse createBook(BookCreateRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException(
                    "A book with ISBN already exists: "
                            + request.getIsbn()
            );
        }

        Book book = Book.builder()
                .title(request.getTitle().trim())
                .isbn(request.getIsbn().trim())
                .author(request.getAuthor().trim())
                .description(request.getDescription())
                .category(request.getCategory().trim())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .status(BookStatus.ACTIVE)
                .build();

        Book savedBook = bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    /**
     * Retrieves a book by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {

        Book book = findBookById(id);

        return mapToResponse(book);
    }

    /**
     * Retrieves all books with pagination and sorting.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                sortDirection
        );

        return bookRepository
                .findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Searches and filters books.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(
            BookSearchRequest request) {

        validateSearchRequest(request);

        Pageable pageable = createPageable(
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortDirection()
        );

        String keyword = normalize(request.getKeyword());
        String category = normalize(request.getCategory());

        return bookRepository
                .searchBooks(
                        keyword,
                        category,
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        pageable
                )
                .map(this::mapToResponse);
    }

    /**
     * Retrieves active books.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getActiveBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                sortDirection
        );

        return bookRepository
                .findByStatus(
                        BookStatus.ACTIVE,
                        pageable
                )
                .map(this::mapToResponse);
    }

    /**
     * Retrieves books that are active and have stock available.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAvailableBooks(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                sortDirection
        );

        return bookRepository
                .findAvailableBooks(
                        BookStatus.ACTIVE,
                        pageable
                )
                .map(this::mapToResponse);
    }

    /**
     * Updates an existing book.
     */
    @Override
    public BookResponse updateBook(
            UUID id,
            BookUpdateRequest request) {

        Book book = findBookById(id);

        /*
         * Update ISBN only when a new value was supplied.
         */
        if (hasText(request.getIsbn())
                && !request.getIsbn().equals(book.getIsbn())) {

            if (bookRepository.existsByIsbn(request.getIsbn())) {
                throw new BadRequestException(
                        "A book with ISBN already exists: "
                                + request.getIsbn()
                );
            }

            book.setIsbn(request.getIsbn().trim());
        }

        /*
         * Update title.
         */
        if (hasText(request.getTitle())) {
            book.setTitle(request.getTitle().trim());
        }

        /*
         * Update author.
         */
        if (hasText(request.getAuthor())) {
            book.setAuthor(request.getAuthor().trim());
        }

        /*
         * Update description.
         */
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }

        /*
         * Update category.
         */
        if (hasText(request.getCategory())) {
            book.setCategory(request.getCategory().trim());
        }

        /*
         * Update price.
         */
        if (request.getPrice() != null) {

            if (request.getPrice().signum() <= 0) {
                throw new BadRequestException(
                        "Price must be greater than zero"
                );
            }

            book.setPrice(request.getPrice());
        }

        /*
         * Update stock.
         *
         * Inventory-specific operations such as increase/decrease
         * stock will be handled by the Inventory module.
         */
        if (request.getStockQuantity() != null) {

            if (request.getStockQuantity() < 0) {
                throw new BadRequestException(
                        "Stock quantity cannot be negative"
                );
            }

            book.setStockQuantity(
                    request.getStockQuantity()
            );
        }

        /*
         * Update image URL.
         */
        if (request.getImageUrl() != null) {
            book.setImageUrl(request.getImageUrl());
        }

        /*
         * Update active/inactive status.
         */
        if (request.getActive() != null) {

            book.setStatus(
                    request.getActive()
                            ? BookStatus.ACTIVE
                            : BookStatus.INACTIVE
            );
        }

        Book updatedBook = bookRepository.save(book);

        return mapToResponse(updatedBook);
    }

    /**
     * Soft deletes a book by marking it INACTIVE.
     */
    @Override
    public void deleteBook(UUID id) {

        Book book = findBookById(id);

        book.setStatus(BookStatus.INACTIVE);

        bookRepository.save(book);
    }

    /**
     * Finds a book or throws a standardized not-found exception.
     */
    private Book findBookById(UUID id) {

        if (id == null) {
            throw new BadRequestException(
                    "Book ID is required"
            );
        }

        return bookRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with ID: " + id
                        )
                );
    }

    /**
     * Creates a Pageable object with validated pagination and sorting.
     */
    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Page size must be between 1 and 100"
            );
        }

        String validSortField = resolveSortField(sortBy);

        Sort.Direction direction =
                resolveSortDirection(sortDirection);

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, validSortField)
        );
    }

    /**
     * Restricts sorting to known Book entity properties.
     *
     * <p>
     * This prevents invalid property names from reaching Spring Data
     * and keeps API sorting predictable.
     * </p>
     */
    private String resolveSortField(String sortBy) {

        if (!hasText(sortBy)) {
            return "createdAt";
        }

        return switch (sortBy.trim()) {

            case "id" -> "id";

            case "title" -> "title";

            case "isbn" -> "isbn";

            case "author" -> "author";

            case "category" -> "category";

            case "price" -> "price";

            case "stockQuantity" -> "stockQuantity";

            case "createdAt" -> "createdAt";

            case "updatedAt" -> "updatedAt";

            case "status" -> "status";

            default -> throw new BadRequestException(
                    "Invalid sort field: " + sortBy
            );
        };
    }

    /**
     * Resolves the requested sorting direction.
     */
    private Sort.Direction resolveSortDirection(
            String sortDirection) {

        if (!hasText(sortDirection)) {
            return Sort.Direction.DESC;
        }

        try {

            return Sort.Direction.fromString(
                    sortDirection.trim()
            );

        } catch (IllegalArgumentException exception) {

            throw new BadRequestException(
                    "Invalid sort direction: "
                            + sortDirection
            );
        }
    }

    /**
     * Validates search-related parameters.
     */
    private void validateSearchRequest(
            BookSearchRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Search request cannot be null"
            );
        }

        if (request.getMinPrice() != null
                && request.getMinPrice().signum() < 0) {

            throw new BadRequestException(
                    "Minimum price cannot be negative"
            );
        }

        if (request.getMaxPrice() != null
                && request.getMaxPrice().signum() < 0) {

            throw new BadRequestException(
                    "Maximum price cannot be negative"
            );
        }

        if (request.getMinPrice() != null
                && request.getMaxPrice() != null
                && request.getMinPrice()
                .compareTo(request.getMaxPrice()) > 0) {

            throw new BadRequestException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        if (request.getMinRating() != null
                && (request.getMinRating() < 0
                || request.getMinRating() > 5)) {

            throw new BadRequestException(
                    "Rating must be between 0 and 5"
            );
        }
    }

    /**
     * Converts a Book entity into a BookResponse DTO.
     */
    private BookResponse mapToResponse(Book book) {

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .imageUrl(book.getImageUrl())
                .averageRating(null)
                .reviewCount(null)
                .active(
                        book.getStatus() == BookStatus.ACTIVE
                )
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Normalizes optional text input.
     */
    private String normalize(String value) {

        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }

    /**
     * Checks whether a string contains meaningful text.
     */
    private boolean hasText(String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}