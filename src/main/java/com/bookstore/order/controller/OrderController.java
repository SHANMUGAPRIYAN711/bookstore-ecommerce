package com.bookstore.order.controller;

import com.bookstore.common.constants.ApiConstants;
import com.bookstore.common.dto.ApiResponse;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummaryResponse;
import com.bookstore.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ORDERS_PATH)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestParam UUID userId,
            @Valid @RequestBody CheckoutRequest request
    ) {

        OrderResponse response =
                orderService.placeOrder(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<OrderResponse>builder()
                                .success(true)
                                .message(
                                        "Order placed successfully"
                                )
                                .data(response)
                                .build()
                );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @RequestParam UUID userId,
            @PathVariable UUID orderId
    ) {

        OrderResponse response =
                orderService.getOrder(
                        userId,
                        orderId
                );

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message(
                                "Order retrieved successfully"
                        )
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<OrderSummaryResponse>>
            > getOrders(
            @RequestParam UUID userId
    ) {

        List<OrderSummaryResponse> response =
                orderService.getOrders(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.<List<OrderSummaryResponse>>builder()
                        .success(true)
                        .message(
                                "Orders retrieved successfully"
                        )
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>>
    updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status
    ) {

        OrderResponse response =
                orderService.updateOrderStatus(
                        orderId,
                        status
                );

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message(
                                "Order status updated successfully"
                        )
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>>
    cancelOrder(
            @RequestParam UUID userId,
            @PathVariable UUID orderId
    ) {

        OrderResponse response =
                orderService.cancelOrder(
                        userId,
                        orderId
                );

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message(
                                "Order cancelled successfully"
                        )
                        .data(response)
                        .build()
        );
    }
}