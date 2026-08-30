package com.bookstore.order.controller;

import com.bookstore.common.dto.ApiResponse;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.OrderItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummaryResponse;
import com.bookstore.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID orderId;
    private UUID bookId;
    private UUID addressId;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(orderController)
                        .build();

        objectMapper =
                new ObjectMapper();

        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        addressId = UUID.randomUUID();
    }

    private OrderItemResponse createItemResponse() {

        return OrderItemResponse.builder()
                .id(UUID.randomUUID())
                .bookId(bookId)
                .bookTitle("Clean Code")
                .unitPrice(new BigDecimal("500.00"))
                .quantity(2)
                .subtotal(new BigDecimal("1000.00"))
                .build();
    }

    private OrderResponse createOrderResponse() {

        return OrderResponse.builder()
                .id(orderId)
                .userId(userId)
                .items(
                        List.of(
                                createItemResponse()
                        )
                )
                .shippingAddressId(addressId)
                .totalAmount(
                        new BigDecimal("1000.00")
                )
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ============================================================
    // PLACE ORDER
    // ============================================================

    @Test
    void placeOrder_shouldReturnCreated() throws Exception {

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        when(orderService.placeOrder(
                any(UUID.class),
                any(CheckoutRequest.class)
        )).thenReturn(
                createOrderResponse()
        );

        mockMvc.perform(
                        post("/api/orders")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order placed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        orderId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.data.paymentStatus")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.data.totalAmount")
                                .value(1000.00)
                );

        /*
         * Spring/Jackson creates a new CheckoutRequest object
         * while deserializing the HTTP request body.
         *
         * Therefore, verify the request using ArgumentCaptor
         * instead of comparing the original Java object.
         */
        ArgumentCaptor<CheckoutRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        CheckoutRequest.class
                );

        verify(orderService)
                .placeOrder(
                        eq(userId),
                        requestCaptor.capture()
                );

        assertEquals(
                addressId,
                requestCaptor.getValue()
                        .getShippingAddressId()
        );
    }

    @Test
    void placeOrder_shouldRejectMissingShippingAddress()
            throws Exception {

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .build();

        mockMvc.perform(
                        post("/api/orders")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .placeOrder(
                        any(UUID.class),
                        any(CheckoutRequest.class)
                );
    }

    @Test
    void placeOrder_shouldRejectInvalidUserId()
            throws Exception {

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        mockMvc.perform(
                        post("/api/orders")
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .placeOrder(
                        any(UUID.class),
                        any(CheckoutRequest.class)
                );
    }

    // ============================================================
    // GET ORDER
    // ============================================================

    @Test
    void getOrder_shouldReturnOrder()
            throws Exception {

        when(orderService.getOrder(
                userId,
                orderId
        )).thenReturn(
                createOrderResponse()
        );

        mockMvc.perform(
                        get(
                                "/api/orders/{orderId}",
                                orderId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        orderId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.items.length()")
                                .value(1)
                );

        verify(orderService)
                .getOrder(
                        userId,
                        orderId
                );
    }

    @Test
    void getOrder_shouldRejectInvalidOrderId()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/orders/{orderId}",
                                "invalid-uuid"
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .getOrder(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    // ============================================================
    // GET USER ORDERS
    // ============================================================

    @Test
    void getOrders_shouldReturnOrderHistory()
            throws Exception {

        OrderSummaryResponse summary =
                OrderSummaryResponse.builder()
                        .id(orderId)
                        .totalAmount(
                                new BigDecimal("1000.00")
                        )
                        .itemCount(2)
                        .orderStatus("PENDING")
                        .paymentStatus("PENDING")
                        .createdAt(Instant.now())
                        .build();

        when(orderService.getOrders(userId))
                .thenReturn(
                        List.of(summary)
                );

        mockMvc.perform(
                        get("/api/orders")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Orders retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].id"
                        ).value(
                                orderId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].itemCount"
                        ).value(2)
                );

        verify(orderService)
                .getOrders(userId);
    }

    @Test
    void getOrders_shouldReturnEmptyList()
            throws Exception {

        when(orderService.getOrders(userId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/orders")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(0)
                );
    }

    @Test
    void getOrders_shouldRejectInvalidUserId()
            throws Exception {

        mockMvc.perform(
                        get("/api/orders")
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .getOrders(any(UUID.class));
    }

    // ============================================================
    // UPDATE ORDER STATUS
    // ============================================================

    @Test
    void updateOrderStatus_shouldReturnSuccess()
            throws Exception {

        when(orderService.updateOrderStatus(
                orderId,
                OrderStatus.CONFIRMED
        )).thenReturn(
                createOrderResponse()
        );

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/status",
                                orderId
                        )
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order status updated successfully"
                                )
                );

        verify(orderService)
                .updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                );
    }

    @Test
    void updateOrderStatus_shouldRejectInvalidOrderId()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/status",
                                "invalid-uuid"
                        )
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .updateOrderStatus(
                        any(UUID.class),
                        any(OrderStatus.class)
                );
    }

    @Test
    void updateOrderStatus_shouldRejectInvalidStatus()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/status",
                                orderId
                        )
                                .param(
                                        "status",
                                        "INVALID"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .updateOrderStatus(
                        any(UUID.class),
                        any(OrderStatus.class)
                );
    }

    // ============================================================
    // CANCEL ORDER
    // ============================================================

    @Test
    void cancelOrder_shouldReturnSuccess()
            throws Exception {

        when(orderService.cancelOrder(
                userId,
                orderId
        )).thenReturn(
                createOrderResponse()
        );

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/cancel",
                                orderId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order cancelled successfully"
                                )
                );

        verify(orderService)
                .cancelOrder(
                        userId,
                        orderId
                );
    }

    @Test
    void cancelOrder_shouldRejectInvalidOrderId()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/cancel",
                                "invalid-uuid"
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .cancelOrder(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void cancelOrder_shouldRejectInvalidUserId()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/cancel",
                                orderId
                        )
                                .param(
                                        "userId",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(orderService, never())
                .cancelOrder(
                        any(UUID.class),
                        any(UUID.class)
                );
    }
}