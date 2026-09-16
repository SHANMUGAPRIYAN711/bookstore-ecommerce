package com.bookstore.notification.producer;

import com.bookstore.config.RabbitMqConfig;
import com.bookstore.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes notification messages to RabbitMQ.
 *
 * <p>
 * This component is responsible only for publishing messages.
 * It does not send emails directly.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    /**
     * Spring AMQP component used to send messages to RabbitMQ.
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a notification message to RabbitMQ.
     *
     * <p>
     * Flow:
     *
     * NotificationProducer
     *        ↓
     * RabbitTemplate
     *        ↓
     * Exchange
     *        ↓
     * Routing Key
     *        ↓
     * Queue
     * </p>
     *
     * @param notification notification message to publish
     */
    public void sendNotification(
            NotificationMessage notification) {

        log.info(
                "Publishing notification | userId={} | recipient={} | type={}",
                notification.getUserId(),
                notification.getRecipientEmail(),
                notification.getType()
        );

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                notification
        );

        log.info(
                "Notification published | recipient={} | type={}",
                notification.getRecipientEmail(),
                notification.getType()
        );
    }
}