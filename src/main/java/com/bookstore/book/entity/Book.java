package com.bookstore.book.entity;

import com.bookstore.common.entity.BaseEntity;
import com.bookstore.common.enums.BookStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a book available in the Bookstore catalog.
 *
 * <p>
 * The Book entity stores the core product information required for
 * catalog browsing, inventory management, cart operations, order
 * processing, wishlist functionality, and product reviews.
 * </p>
 *
 * <p>
 * Inventory-related information such as the current stock quantity
 * is maintained on this entity because stock availability is a
 * fundamental property of the book being sold.
 * </p>
 *
 * <p>
 * The entity also stores the S3 object key and URL associated with
 * the book's cover image. The actual image binary data is not stored
 * inside PostgreSQL.
 * </p>
 */
@Entity
@Table(
        name = "books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_books_isbn",
                        columnNames = "isbn"
                )
        },
        indexes = {
                @Index(
                        name = "idx_books_title",
                        columnList = "title"
                ),
                @Index(
                        name = "idx_books_author",
                        columnList = "author"
                ),
                @Index(
                        name = "idx_books_category",
                        columnList = "category"
                ),
                @Index(
                        name = "idx_books_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book extends BaseEntity {

    /**
     * Title of the book.
     */
    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    /**
     * International Standard Book Number used to uniquely identify
     * the book edition.
     */
    @Column(
            name = "isbn",
            nullable = false,
            length = 20
    )
    private String isbn;

    /**
     * Name of the primary author of the book.
     */
    @Column(
            name = "author",
            nullable = false,
            length = 200
    )
    private String author;

    /**
     * Detailed description of the book.
     */
    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    /**
     * Category or genre under which the book is classified.
     */
    @Column(
            name = "category",
            nullable = false,
            length = 100
    )
    private String category;

    /**
     * Selling price of the book.
     *
     * <p>
     * BigDecimal is used instead of floating-point types to avoid
     * precision problems when representing monetary values.
     * </p>
     */
    @Column(
            name = "price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal price;

    /**
     * Number of units currently available in inventory.
     */
    @Column(
            name = "stock_quantity",
            nullable = false
    )
    private Integer stockQuantity;

    /**
     * Public URL of the book cover image stored in AWS S3.
     */
    @Column(
            name = "image_url",
            length = 1000
    )
    private String imageUrl;

    /**
     * Object key of the book cover image within the configured
     * AWS S3 bucket.
     *
     * <p>
     * The key is used by the storage service when replacing or
     * deleting the corresponding S3 object.
     * </p>
     */
    @Column(
            name = "image_key",
            length = 500
    )
    private String imageKey;

    /**
     * Current catalog status of the book.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private BookStatus status = BookStatus.ACTIVE;
}