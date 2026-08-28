package com.bookstore.order.entity;

import com.bookstore.book.entity.Book;
import com.bookstore.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents one purchased book within a customer order.
 *
 * <p>
 * An OrderItem connects an Order with a Book and records the quantity
 * purchased and the price applicable when the order was created.
 * </p>
 *
 * <p>
 * The unit price is intentionally stored independently from the
 * current Book price so that historical orders remain financially
 * accurate after future product price changes.
 * </p>
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    /**
     * Order to which this item belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    /**
     * Book purchased by the customer.
     *
     * <p>
     * The relationship preserves the reference to the catalog product,
     * while unitPrice preserves the historical selling price.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    /**
     * Number of copies of the book purchased.
     */
    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    /**
     * Price of one unit at the time the order was placed.
     */
    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    /**
     * Total price for this order item.
     *
     * <p>
     * This value is calculated as:
     * quantity multiplied by unitPrice.
     * </p>
     */
    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;
}