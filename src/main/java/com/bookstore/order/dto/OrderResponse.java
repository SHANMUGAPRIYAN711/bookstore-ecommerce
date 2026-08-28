package com.bookstore.order.dto;

import com.bookstore.common.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detailed response DTO representing a customer order.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * Order identifier.
     */
    private UUID id;

    /**
     * User identifier associated with the order.
     */
    private UUID userId;

    /**
     * Items purchased in the order.
     */
    private List<OrderItemResponse> items;

    /**
     * Shipping address identifier.
     */
    private UUID shippingAddressId;

    /**
     * Total monetary value of the order.
     */
    private BigDecimal totalAmount;

    /**
     * Current order status.
     */
    private String orderStatus;

    /**
     * Current payment status.
     */
    private String paymentStatus;

    /**
     * Order creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last order modification timestamp.
     */
    private Instant updatedAt;
}