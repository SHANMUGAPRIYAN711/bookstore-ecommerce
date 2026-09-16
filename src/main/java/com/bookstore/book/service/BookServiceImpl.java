package com.bookstore.book.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.book.dto.BookCacheData;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link BookService} interface.
 *
 * <p>
 * This service handles book creation, retrieval, searching, updating,
 * and soft deletion.
 * </p>
 *
 * <p>
 * PostgreSQL remains the primary source of truth for book data.
 * Redis is used as a cache for individual book-detail requests to
 * reduce repeated database access.
 * </p>
 *
 * <p>
 * AWS S3 presigned URLs are generated dynamically and are never
 * stored in Redis because presigned URLs are temporary.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    /**
     * Repository used to persist and retrieve books.
     */
    private final BookRepository bookRepository;

    /**
     * Storage service used to generate temporary S3 presigned URLs.
     */
    private final StorageService storageService;

    /**
     * Service responsible for Redis book-cache operations.
     */
    private final BookCacheService bookCacheService;

    /**
     * Creates a new book.
     *
     * <p>
     * ISBN uniqueness is checked before the book is persisted.
     * Newly created books are assigned an ACTIVE status.
     * </p>
     *
     * @param request book creation request
     * @return created book response
     * @throws BadRequestException if a book with the same ISBN already exists
     */
    @Auditable(
            action = "CREATE_BOOK",
            entity = "BOOK"
    )
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
                .imageKey(request.getImageKey())
                .status(BookStatus.ACTIVE)
                .build();

        Book savedBook = bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    /**
     * Retrieves a book by its ID.
     *
     * <p>
     * Redis is checked before PostgreSQL. If the book exists in Redis,
     * the cached data is returned and a fresh S3 presigned URL is
     * generated.
     * </p>
     *
     * <p>
     * If Redis does not contain the book, PostgreSQL is queried.
     * The result is then stored in Redis for subsequent requests.
     * </p>
     *
     * @param id unique identifier of the book
     * @return book response
     * @throws BadRequestException if the book ID is null
     * @throws ResourceNotFoundException if the book does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {

        if (id == null) {
            throw new BadRequestException("Book ID is required");
        }

        /*
         * Step 1:
         * Check Redis before querying PostgreSQL.
         */
        Optional<BookCacheData> cachedBook =
                bookCacheService.getBook(id);

        /*
         * Step 2:
         * Cache HIT.
         *
         * Convert cached data directly into a response.
         * A fresh S3 presigned URL is generated here.
         */
        if (cachedBook.isPresent()) {

            return mapCacheDataToResponse(
                    cachedBook.get()
            );
        }

        /*
         * Step 3:
         * Cache MISS.
         *
         * Retrieve the book from PostgreSQL.
         */
        Book book = findBookById(id);

        /*
         * Step 4:
         * Store the database result in Redis.
         *
         * Only persistent book data is cached.
         * The temporary S3 URL is intentionally excluded.
         */
        bookCacheService.putBook(
                mapToCacheData(book)
        );

        /*
         * Step 5:
         * Return the response with a fresh S3 presigned URL.
         */
        return mapToResponse(book);
    }

    /**
     * Retrieves all books with pagination and sorting.
     *
     * @param page page number
     * @param size number of records per page
     * @param sortBy property used for sorting
     * @param sortDirection sorting direction
     * @return paginated list of books
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
     *
     * @param request search and filter criteria
     * @return paginated search results
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
     *
     * @param page page number
     * @param size number of records per page
     * @param sortBy property used for sorting
     * @param sortDirection sorting direction
     * @return paginated list of active books
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
     *
     * @param page page number
     * @param size number of records per page
     * @param sortBy property used for sorting
     * @param sortDirection sorting direction
     * @return paginated list of available books
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
     *
     * <p>
     * Only fields supplied in the update request are changed.
     * After the database update succeeds, the corresponding Redis
     * cache entry is evicted to prevent stale book data.
     * </p>
     *
     * @param id unique identifier of the book
     * @param request book update request
     * @return updated book response
     * @throws ResourceNotFoundException if the book does not exist
     * @throws BadRequestException if validation fails
     */
    @Auditable(
            action = "UPDATE_BOOK",
            entity = "BOOK"
    )
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
         * Inventory-specific increase/decrease operations
         * are handled by the Inventory module.
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
         * Update book cover image reference.
         */
        if (request.getImageKey() != null) {

            book.setImageKey(
                    request.getImageKey().trim()
            );
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

        /*
         * Save the latest book state to PostgreSQL.
         */
        Book updatedBook = bookRepository.save(book);

        /*
         * Evict the old Redis entry.
         *
         * The next GET request will fetch the latest version
         * from PostgreSQL and cache it again.
         */
        bookCacheService.evictBook(id);

        return mapToResponse(updatedBook);
    }

    /**
     * Soft deletes a book by marking it INACTIVE.
     *
     * <p>
     * The corresponding Redis cache entry is evicted after the
     * database update.
     * </p>
     *
     * @param id unique identifier of the book
     * @throws ResourceNotFoundException if the book does not exist
     */
    @Auditable(
            action = "DELETE_BOOK",
            entity = "BOOK"
    )
    @Override
    public void deleteBook(UUID id) {

        Book book = findBookById(id);

        book.setStatus(BookStatus.INACTIVE);

        bookRepository.save(book);

        /*
         * Remove the stale book from Redis.
         */
        bookCacheService.evictBook(id);
    }

    /**
     * Finds a book by ID.
     *
     * @param id unique identifier of the book
     * @return book entity
     * @throws BadRequestException if ID is null
     * @throws ResourceNotFoundException if the book does not exist
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
     * Creates a Pageable object with validated pagination
     * and sorting configuration.
     *
     * @param page page number
     * @param size page size
     * @param sortBy property used for sorting
     * @param sortDirection sorting direction
     * @return configured Pageable instance
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
     * @param sortBy requested sorting property
     * @return validated entity property name
     * @throws BadRequestException if the property is not supported
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
     *
     * @param sortDirection requested sorting direction
     * @return validated Sort.Direction
     * @throws BadRequestException if the direction is invalid
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
     *
     * @param request search request
     * @throws BadRequestException if any search parameter is invalid
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
     * Converts a Book entity into data suitable for Redis.
     *
     * <p>
     * The S3 presigned image URL is intentionally excluded because
     * presigned URLs are temporary. The persistent S3 object key
     * is stored instead.
     * </p>
     *
     * @param book book entity
     * @return Redis cache representation
     */
    private BookCacheData mapToCacheData(Book book) {

        return BookCacheData.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .imageKey(book.getImageKey())
                .averageRating(null)
                .reviewCount(null)
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Converts cached book data into a BookResponse DTO.
     *
     * <p>
     * A fresh AWS S3 presigned URL is generated whenever a cached
     * book is returned.
     * </p>
     *
     * @param book cached book data
     * @return book response DTO
     */
    private BookResponse mapCacheDataToResponse(
            BookCacheData book) {

        String imageUrl = null;

        if (book.getImageKey() != null
                && !book.getImageKey().isBlank()) {

            imageUrl =
                    storageService.generatePresignedUrl(
                            book.getImageKey()
                    );
        }

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .imageUrl(imageUrl)
                .imageKey(book.getImageKey())
                .averageRating(book.getAverageRating())
                .reviewCount(book.getReviewCount())
                .active(
                        book.getStatus() ==
                                BookStatus.ACTIVE
                )
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Converts a Book entity into a BookResponse DTO.
     *
     * <p>
     * The persisted S3 object key is converted into a temporary
     * presigned URL so that clients can access the private book
     * cover image.
     * </p>
     *
     * @param book book entity
     * @return book response DTO
     */
    private BookResponse mapToResponse(
            Book book) {

        String imageUrl = null;

        if (book.getImageKey() != null
                && !book.getImageKey().isBlank()) {

            imageUrl =
                    storageService.generatePresignedUrl(
                            book.getImageKey()
                    );
        }

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .imageUrl(imageUrl)
                .imageKey(book.getImageKey())
                .averageRating(null)
                .reviewCount(null)
                .active(
                        book.getStatus() ==
                                BookStatus.ACTIVE
                )
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Normalizes optional text input.
     *
     * @param value input text
     * @return trimmed text or null when blank
     */
    private String normalize(String value) {

        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }

    /**
     * Checks whether a string contains meaningful text.
     *
     * @param value string to validate
     * @return true when the value contains non-whitespace characters
     */
    private boolean hasText(String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}