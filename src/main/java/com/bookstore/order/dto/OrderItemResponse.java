package com.bookstore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO representing one item belonging to an order.
 *
 * <p>
 * Product information is stored as an order snapshot so that historical
 * order information remains meaningful even when the book catalog changes.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    /**
     * Order item identifier.
     */
    private UUID id;

    /**
     * Original book identifier.
     */
    private UUID bookId;

    /**
     * Book title captured when the order was placed.
     */
    private String bookTitle;

    /**
     * Price per item captured at checkout.
     */
    private BigDecimal unitPrice;

    /**
     * Quantity purchased.
     */
    private int quantity;

    /**
     * Total value of this order item.
     */
    private BigDecimal subtotal;
}