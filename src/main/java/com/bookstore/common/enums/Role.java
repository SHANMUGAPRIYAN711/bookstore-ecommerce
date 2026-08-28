package com.bookstore.common.enums;

/**
 * Defines the roles available to users within the Bookstore application.
 */
public enum Role {

    /**
     * Standard customer who can browse and purchase books.
     */
    CUSTOMER,

    /**
     * Administrator with full administrative privileges.
     */
    ADMIN,

    /**
     * Helper responsible primarily for inventory-related operations.
     */
    HELPER
}