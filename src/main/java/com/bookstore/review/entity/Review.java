package com.bookstore.review.entity;

import com.bookstore.book.entity.Book;
import com.bookstore.common.entity.BaseEntity;
import com.bookstore.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a product review submitted by a user for a book.
 *
 * <p>
 * A review contains a numerical rating and an optional textual comment.
 * Each review belongs to exactly one user and exactly one book.
 * </p>
 *
 * <p>
 * The database enforces the business rule that a user can submit
 * only one review for a particular book by using a composite unique
 * constraint on user_id and book_id.
 * </p>
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reviews_user_book",
                        columnNames = {
                                "user_id",
                                "book_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    /**
     * User who submitted the review.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Book being reviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    /**
     * Numerical rating assigned to the book.
     *
     * <p>
     * The valid range is one through five. Request-level validation
     * will enforce this constraint before persistence.
     * </p>
     */
    @Column(
            name = "rating",
            nullable = false
    )
    private Integer rating;

    /**
     * Optional textual feedback provided by the user.
     */
    @Column(
            name = "comment",
            columnDefinition = "TEXT"
    )
    private String comment;
}