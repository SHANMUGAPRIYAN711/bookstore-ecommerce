package com.bookstore.common.enums;

/**
 * Defines the supported types of addresses that a user can maintain
 * in the Bookstore application.
 *
 * <p>
 * An address type allows the application to distinguish between
 * common address purposes such as a home address, work address,
 * or another delivery address.
 * </p>
 */
public enum AddressType {

    /**
     * Represents the user's home or residential address.
     */
    HOME,

    /**
     * Represents the user's workplace or business address.
     */
    WORK,

    /**
     * Represents another address that does not fall under the
     * HOME or WORK categories.
     */
    OTHER
}