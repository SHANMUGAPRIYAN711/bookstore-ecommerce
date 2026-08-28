package com.bookstore.common.enums;

/**
 * Defines the lifecycle states of a user account.
 */
public enum UserStatus {

    /**
     * User account is active and can authenticate normally.
     */
    ACTIVE,

    /**
     * User account is temporarily inactive.
     */
    INACTIVE,

    /**
     * User account is blocked from accessing protected functionality.
     */
    BLOCKED
}