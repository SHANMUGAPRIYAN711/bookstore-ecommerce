package com.bookstore.common.constants;

/**
 * Contains constants used for RabbitMQ exchanges, queues,
 * and routing keys throughout the application.
 */
public final class MessagingConstants {

    /**
     * Prevents instantiation of this utility class.
     */
    private MessagingConstants() {
    }

    /**
     * Exchange used for bookstore application events.
     */
    public static final String BOOKSTORE_EXCHANGE =
            "bookstore.exchange";

    /**
     * Queue used for notification processing.
     */
    public static final String NOTIFICATION_QUEUE =
            "bookstore.notification.queue";

    /**
     * Queue used for order processing.
     */
    public static final String ORDER_QUEUE =
            "bookstore.order.queue";

    /**
     * Queue used for inventory processing.
     */
    public static final String INVENTORY_QUEUE =
            "bookstore.inventory.queue";

    /**
     * Routing key for order events.
     */
    public static final String ORDER_ROUTING_KEY =
            "order.event";

    /**
     * Routing key for notification events.
     */
    public static final String NOTIFICATION_ROUTING_KEY =
            "notification.event";

    /**
     * Routing key for inventory events.
     */
    public static final String INVENTORY_ROUTING_KEY =
            "inventory.event";
}