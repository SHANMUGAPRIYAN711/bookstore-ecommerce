package com.bookstore.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Message published to RabbitMQ for email notification processing.
 *
 * <p>
 * This DTO represents the message transferred from the
 * RabbitMQ producer to the RabbitMQ consumer.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /**
     * Identifier of the user receiving the notification.
     */
    private UUID userId;

    /**
     * Email address of the recipient.
     */
    private String recipientEmail;

    /**
     * Subject/title of the email.
     */
    private String title;

    /**
     * Body of the email.
     */
    private String message;

    /**
     * Type of notification.
     *
     * <p>
     * Examples:
     * ORDER_CREATED,
     * ORDER_CONFIRMED,
     * ORDER_CANCELLED,
     * ORDER_SHIPPED,
     * WELCOME_EMAIL,
     * PASSWORD_RESET.
     * </p>
     */
    private String type;
}