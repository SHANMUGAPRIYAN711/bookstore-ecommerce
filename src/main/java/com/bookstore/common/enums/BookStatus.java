package com.bookstore.common.enums;

/**
 * Defines the lifecycle states of a book in the Bookstore catalog.
 */
public enum BookStatus {

    /**
     * Indicates that the book is active and visible in the catalog.
     */
    ACTIVE,

    /**
     * Indicates that the book is inactive and should not normally
     * be available for customer purchase.
     */
    INACTIVE
}