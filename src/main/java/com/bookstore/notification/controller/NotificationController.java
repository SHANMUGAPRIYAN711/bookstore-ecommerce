package com.bookstore.notification.controller;

import com.bookstore.notification.dto.NotificationMessage;
import com.bookstore.notification.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller used to test the notification flow.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationProducer notificationProducer;

    /**
     * Publishes a test email notification to RabbitMQ.
     *
     * @param recipientEmail email address that should receive the test email
     * @return confirmation that the message was published
     */
    @PostMapping("/test-email")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam String recipientEmail) {

        NotificationMessage notification =
                NotificationMessage.builder()
                        .userId(UUID.randomUUID())
                        .recipientEmail(recipientEmail)
                        .title("Bookstore Test Email")
                        .message(
                                "This is a test email sent through " +
                                        "RabbitMQ and Gmail SMTP."
                        )
                        .type("TEST_EMAIL")
                        .build();

        notificationProducer.sendNotification(
                notification
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        "Notification published to RabbitMQ successfully"
                );
    }
}