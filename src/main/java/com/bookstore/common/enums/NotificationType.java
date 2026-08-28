package com.bookstore.common.enums;

/**
 * Defines the types of notifications
 * supported by the application.
 */
public enum NotificationType {

    ORDER_CREATED,

    ORDER_CONFIRMED,

    ORDER_SHIPPED,

    ORDER_DELIVERED,

    ORDER_CANCELLED,

    PAYMENT_SUCCESS,

    PAYMENT_FAILED,

    STOCK_LOW,

    GENERAL
}