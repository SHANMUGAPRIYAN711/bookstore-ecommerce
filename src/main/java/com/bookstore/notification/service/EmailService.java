package com.bookstore.notification.service;

/**
 * Defines email-related operations used by the notification module.
 */
public interface EmailService {

    /**
     * Sends a plain-text email.
     *
     * @param recipientEmail recipient email address
     * @param subject email subject
     * @param message email body
     */
    void sendEmail(
            String recipientEmail,
            String subject,
            String message
    );
}