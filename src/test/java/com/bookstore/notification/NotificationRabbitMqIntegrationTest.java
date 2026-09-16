package com.bookstore.notification;

import com.bookstore.notification.dto.NotificationMessage;
import com.bookstore.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.bookstore.notification.service.EmailServiceImpl;

import java.util.UUID;

import static com.bookstore.config.RabbitMqConfig.NOTIFICATION_EXCHANGE;
import static com.bookstore.config.RabbitMqConfig.NOTIFICATION_ROUTING_KEY;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Integration test that verifies the RabbitMQ notification flow.
 *
 * <p>
 * The test uses the real RabbitMQ broker running in Docker.
 * RabbitMQ producer and consumer remain real.
 * Only EmailService is mocked so that no real email is sent.
 * </p>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest",

        /*
         * Dummy mail configuration.
         *
         * EmailService is mocked, so no real email is sent.
         */
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password"
})
class NotificationRabbitMqIntegrationTest {

    /**
     * Real RabbitTemplate used to publish messages
     * to the RabbitMQ exchange.
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Mocked EmailService.
     *
     * <p>
     * RabbitMQ, exchange, queue and NotificationConsumer
     * remain real. Only the final email-sending operation
     * is mocked.
     * </p>
     */
    @MockitoBean
    private EmailServiceImpl emailService;

    /**
     * Clears previous interactions with the mocked EmailService
     * before each test.
     */
    @BeforeEach
    void setUp() {
        clearInvocations(emailService);
    }

    /**
     * Verifies that a notification published to RabbitMQ
     * is consumed by NotificationConsumer and forwarded
     * to EmailService with the correct data.
     */
    @Test
    void rabbitMq_shouldDeliverNotificationToConsumer() {

        // Arrange
        NotificationMessage notification =
                NotificationMessage.builder()
                        .userId(UUID.randomUUID())
                        .recipientEmail("customer@gmail.com")
                        .title("Order Confirmed")
                        .message(
                                "Your order has been confirmed successfully."
                        )
                        .type("ORDER_CONFIRMED")
                        .build();

        // Act
        /*
         * Publish the notification to the REAL RabbitMQ broker.
         *
         * Flow:
         *
         * RabbitTemplate
         *      ↓
         * Exchange
         *      ↓
         * Routing Key
         *      ↓
         * Queue
         *      ↓
         * NotificationConsumer
         *      ↓
         * EmailService
         */
        rabbitTemplate.convertAndSend(
                NOTIFICATION_EXCHANGE,
                NOTIFICATION_ROUTING_KEY,
                notification
        );

        // Assert
        /*
         * RabbitMQ processing is asynchronous.
         *
         * Wait up to 10 seconds for NotificationConsumer
         * to receive the message and call EmailService.
         *
         * Exact arguments are verified so an unrelated
         * old notification cannot make this test pass.
         */
        verify(
                emailService,
                timeout(10_000)
        ).sendEmail(
                "customer@gmail.com",
                "Order Confirmed",
                "Your order has been confirmed successfully."
        );
    }
}