package com.bookstore.exception;

/**
 * Exception thrown when an authenticated user does not have
 * sufficient permissions to access a requested resource or operation.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Creates a new ForbiddenException with the supplied error message.
     *
     * @param message descriptive message explaining why access is forbidden
     */
    public ForbiddenException(String message) {
        super(message);
    }
}