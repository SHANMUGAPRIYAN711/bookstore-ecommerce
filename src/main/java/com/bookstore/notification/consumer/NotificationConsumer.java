package com.bookstore.notification.consumer;

import com.bookstore.notification.dto.NotificationMessage;
import com.bookstore.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.bookstore.config.RabbitMqConfig.NOTIFICATION_QUEUE;

/**
 * RabbitMQ consumer responsible for processing notification messages.
 *
 * <p>
 * The consumer receives a notification from RabbitMQ and
 * delegates email delivery to the EmailService.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    /**
     * Service responsible for sending the email.
     */
    private final EmailService emailService;

    /**
     * Receives notification messages from RabbitMQ.
     *
     * <p>
     * Flow:
     *
     * RabbitMQ Queue
     *        ↓
     * NotificationConsumer
     *        ↓
     * EmailService
     *        ↓
     * Gmail SMTP
     * </p>
     *
     * @param notification notification received from RabbitMQ
     */
    @RabbitListener(queues = NOTIFICATION_QUEUE)
    public void consumeNotification(
            NotificationMessage notification) {

        log.info(
                "NOTIFICATION RECEIVED | userId={} | email={} | type={} | title={}",
                notification.getUserId(),
                notification.getRecipientEmail(),
                notification.getType(),
                notification.getTitle()
        );

        emailService.sendEmail(
                notification.getRecipientEmail(),
                notification.getTitle(),
                notification.getMessage()
        );

        log.info(
                "NOTIFICATION PROCESSED | email={} | type={}",
                notification.getRecipientEmail(),
                notification.getType()
        );
    }
}