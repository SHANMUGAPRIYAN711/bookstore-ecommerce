package com.bookstore.wishlist.entity;

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
 * Represents a book saved by a user in their wishlist.
 *
 * <p>
 * A wishlist item creates an association between a user and a book
 * that the user wants to keep for future reference or purchase.
 * </p>
 *
 * <p>
 * A user cannot add the same book to their wishlist more than once.
 * This rule is enforced at the database level through a composite
 * unique constraint on user_id and book_id.
 * </p>
 */
@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wishlist_user_book",
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
public class WishlistItem extends BaseEntity {

    /**
     * User who owns this wishlist item.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Book saved by the user.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;
}