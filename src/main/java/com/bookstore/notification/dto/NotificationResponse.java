package com.bookstore.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a notification delivered to a user.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /**
     * Notification identifier.
     */
    private UUID id;

    /**
     * User receiving the notification.
     */
    private UUID userId;

    /**
     * Notification title.
     */
    private String title;

    /**
     * Notification message.
     */
    private String message;

    /**
     * Notification type.
     */
    private String type;

    /**
     * Indicates whether the notification has been read.
     */
    private boolean read;

    /**
     * Timestamp at which the notification was created.
     */
    private Instant createdAt;
}