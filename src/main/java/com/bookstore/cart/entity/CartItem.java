package com.bookstore.cart.entity;

import com.bookstore.book.entity.Book;
import com.bookstore.common.entity.BaseEntity;
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
 * Represents one book entry within a user's shopping cart.
 *
 * <p>
 * A CartItem connects a Cart with a Book and stores the quantity
 * requested by the customer.
 * </p>
 *
 * <p>
 * A particular book can appear only once within a given cart.
 * If the customer adds the same book again, the service layer will
 * increase the existing quantity instead of creating another
 * CartItem record.
 * </p>
 */
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_items_cart_book",
                        columnNames = {
                                "cart_id",
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
public class CartItem extends BaseEntity {

    /**
     * Shopping cart containing this item.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    private Cart cart;

    /**
     * Book selected by the customer.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    /**
     * Number of copies of the book requested by the customer.
     */
    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;
}