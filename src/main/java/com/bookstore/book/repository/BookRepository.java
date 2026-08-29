package com.bookstore.book.repository;

import com.bookstore.book.entity.Book;
import com.bookstore.common.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    /**
     * Finds a book by its ISBN.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Checks whether a book already exists with the given ISBN.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Finds books by their catalog status.
     */
    Page<Book> findByStatus(
            BookStatus status,
            Pageable pageable
    );

    /**
     * Finds books by category.
     */
    Page<Book> findByCategoryIgnoreCase(
            String category,
            Pageable pageable
    );

    /**
     * Finds books by author.
     */
    Page<Book> findByAuthorContainingIgnoreCase(
            String author,
            Pageable pageable
    );

    /**
     * Finds books by title.
     */
    Page<Book> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    /**
     * Searches books using keyword, category and optional price range.
     *
     * <p>
     * The keyword is matched against:
     * title,
     * author,
     * ISBN,
     * and category.
     * </p>
     *
     * <p>
     * Category, minimum price and maximum price are optional.
     * </p>
     */
    @Query("""
            SELECT b
            FROM Book b
            WHERE
                (
                    :keyword IS NULL
                    OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (
                    :category IS NULL
                    OR LOWER(b.category) = LOWER(:category)
                )
                AND (
                    :minPrice IS NULL
                    OR b.price >= :minPrice
                )
                AND (
                    :maxPrice IS NULL
                    OR b.price <= :maxPrice
                )
            """)
    Page<Book> searchBooks(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Finds books that are active and have stock available.
     */
    @Query("""
            SELECT b
            FROM Book b
            WHERE b.status = :status
              AND b.stockQuantity > 0
            """)
    Page<Book> findAvailableBooks(
            @Param("status") BookStatus status,
            Pageable pageable
    );
}