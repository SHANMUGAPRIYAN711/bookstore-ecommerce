package com.bookstore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight order representation used when displaying lists of
 * previous orders.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    /**
     * Order identifier.
     */
    private UUID id;

    /**
     * Total amount of the order.
     */
    private BigDecimal totalAmount;

    /**
     * Number of items in the order.
     */
    private int itemCount;

    /**
     * Current order status.
     */
    private String orderStatus;

    /**
     * Current payment status.
     */
    private String paymentStatus;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;
}