package com.bookstore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE =
            "bookstore.notification.exchange";

    public static final String NOTIFICATION_QUEUE =
            "bookstore.notification.queue";

    public static final String NOTIFICATION_ROUTING_KEY =
            "notification";

    /**
     * Creates the notification exchange.
     */
    @Bean
    public DirectExchange notificationExchange() {

        return new DirectExchange(
                NOTIFICATION_EXCHANGE
        );
    }

    /**
     * Creates the notification queue.
     *
     * <p>
     * The queue is durable so that it survives a RabbitMQ
     * broker restart.
     * </p>
     */
    @Bean
    public Queue notificationQueue() {

        return new Queue(
                NOTIFICATION_QUEUE,
                true
        );
    }

    /**
     * Binds the notification queue to the notification exchange.
     *
     * <p>
     * Because this is a DirectExchange, the routing key must
     * exactly match the routing key used by the producer.
     * </p>
     */
    @Bean
    public Binding notificationBinding(
            Queue notificationQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * Converts Java objects into JSON messages and JSON messages
     * back into Java objects.
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures RabbitTemplate to use JSON conversion.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {

        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(
                messageConverter
        );

        return rabbitTemplate;
    }
}