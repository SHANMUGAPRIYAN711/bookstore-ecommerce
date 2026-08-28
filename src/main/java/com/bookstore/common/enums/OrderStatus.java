package com.bookstore.common.enums;

/**
 * Defines the lifecycle states through which a customer order can progress.
 */
public enum OrderStatus {

    /**
     * Order has been created but has not yet been confirmed.
     */
    PENDING,

    /**
     * Order has been successfully confirmed.
     */
    CONFIRMED,

    /**
     * Order is currently being prepared for shipment.
     */
    PROCESSING,

    /**
     * Order has been dispatched to the customer.
     */
    SHIPPED,

    /**
     * Order has been successfully delivered.
     */
    DELIVERED,

    /**
     * Order has been cancelled.
     */
    CANCELLED
}