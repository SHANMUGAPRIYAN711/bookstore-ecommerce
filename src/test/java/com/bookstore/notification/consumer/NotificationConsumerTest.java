package com.bookstore.notification.consumer;

import com.bookstore.notification.dto.NotificationMessage;
import com.bookstore.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationMessage notification;

    @BeforeEach
    void setUp() {

        notification = NotificationMessage.builder()
                .userId(UUID.randomUUID())
                .recipientEmail("customer@gmail.com")
                .title("Order Confirmed")
                .message(
                        "Your order has been confirmed successfully."
                )
                .type("ORDER_CONFIRMED")
                .build();
    }

    @Test
    void consumeNotification_shouldSendEmail() {

        notificationConsumer.consumeNotification(
                notification
        );

        verify(emailService, times(1))
                .sendEmail(
                        notification.getRecipientEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                );
    }

    @Test
    void consumeNotification_shouldSendCorrectEmailDetails() {

        notificationConsumer.consumeNotification(
                notification
        );

        ArgumentCaptor<String> recipientCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> subjectCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService)
                .sendEmail(
                        recipientCaptor.capture(),
                        subjectCaptor.capture(),
                        messageCaptor.capture()
                );

        assertEquals(
                "customer@gmail.com",
                recipientCaptor.getValue()
        );

        assertEquals(
                "Order Confirmed",
                subjectCaptor.getValue()
        );

        assertEquals(
                "Your order has been confirmed successfully.",
                messageCaptor.getValue()
        );
    }
}