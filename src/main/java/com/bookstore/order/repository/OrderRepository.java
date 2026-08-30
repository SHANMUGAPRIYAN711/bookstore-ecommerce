package com.bookstore.order.repository;

import com.bookstore.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndUserId(
            UUID orderId,
            UUID userId
    );

    List<Order> findByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    boolean existsByOrderNumber(
            String orderNumber
    );
}