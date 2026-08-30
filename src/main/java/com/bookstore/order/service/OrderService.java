package com.bookstore.order.service;

import com.bookstore.common.enums.OrderStatus;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(
            UUID userId,
            CheckoutRequest request
    );

    OrderResponse getOrder(
            UUID userId,
            UUID orderId
    );

    List<OrderSummaryResponse> getOrders(
            UUID userId
    );

    OrderResponse updateOrderStatus(
            UUID orderId,
            OrderStatus status
    );

    OrderResponse cancelOrder(
            UUID userId,
            UUID orderId
    );
}