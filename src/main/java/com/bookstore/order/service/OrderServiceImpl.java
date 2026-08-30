package com.bookstore.order.service;

import com.bookstore.address.entity.Address;
import com.bookstore.address.repository.AddressRepository;
import com.bookstore.audit.annotation.Auditable;
import com.bookstore.book.entity.Book;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.common.enums.PaymentStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.OrderItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummaryResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal SHIPPING_FEE =
            BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;

    @Override
    @Auditable(
            action = "CREATE_ORDER",
            entity = "ORDER"
    )
    public OrderResponse placeOrder(
            UUID userId,
            CheckoutRequest request
    ) {

        if (request == null ||
                request.getShippingAddressId() == null) {

            throw new BadRequestException(
                    "Shipping address is required"
            );
        }

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        Address address =
                addressRepository.findByIdAndUserId(
                                request.getShippingAddressId(),
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shipping address not found"
                                )
                        );

        Cart cart =
                cartRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        if (cart.getItems() == null ||
                cart.getItems().isEmpty()) {

            throw new BadRequestException(
                    "Cannot place order with an empty cart"
            );
        }

        Order order =
                Order.builder()
                        .user(user)
                        .orderNumber(
                                generateUniqueOrderNumber()
                        )
                        .status(OrderStatus.PENDING)
                        .subtotal(BigDecimal.ZERO)
                        .shippingFee(SHIPPING_FEE)
                        .totalAmount(BigDecimal.ZERO)
                        .shippingAddressLine1(
                                address.getAddressLine1()
                        )
                        .shippingAddressLine2(
                                address.getAddressLine2()
                        )
                        .shippingCity(
                                address.getCity()
                        )
                        .shippingState(
                                address.getState()
                        )
                        .shippingPostalCode(
                                address.getPostalCode()
                        )
                        .shippingCountry(
                                address.getCountry()
                        )
                        .items(new ArrayList<>())
                        .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            if (cartItem.getBook() == null) {

                throw new BadRequestException(
                        "Cart contains an invalid book"
                );
            }

            if (cartItem.getQuantity() == null ||
                    cartItem.getQuantity() <= 0) {

                throw new BadRequestException(
                        "Invalid cart item quantity"
                );
            }

            Book book = cartItem.getBook();

            if (book.getPrice() == null) {

                throw new BadRequestException(
                        "Book price is not available"
                );
            }

            boolean sufficientStock =
                    inventoryService.hasSufficientStock(
                            book.getId(),
                            cartItem.getQuantity()
                    );

            if (!sufficientStock) {

                throw new BadRequestException(
                        "Insufficient stock for book: "
                                + book.getTitle()
                );
            }

            BigDecimal unitPrice =
                    book.getPrice();

            BigDecimal itemSubtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            OrderItem orderItem =
                    OrderItem.builder()
                            .order(order)
                            .book(book)
                            .quantity(
                                    cartItem.getQuantity()
                            )
                            .unitPrice(unitPrice)
                            .subtotal(itemSubtotal)
                            .build();

            order.getItems().add(orderItem);

            subtotal =
                    subtotal.add(itemSubtotal);
        }

        BigDecimal totalAmount =
                subtotal.add(SHIPPING_FEE);

        order.setSubtotal(subtotal);
        order.setTotalAmount(totalAmount);

        Order savedOrder =
                orderRepository.save(order);

        /*
         * Deduct inventory for every order item.
         */
        for (OrderItem orderItem :
                savedOrder.getItems()) {

            inventoryService.decreaseStock(
                    orderItem.getBook().getId(),
                    orderItem.getQuantity()
            );
        }

        /*
         * Clear the cart after successful
         * order creation and stock deduction.
         */
        cart.getItems().clear();

        cartRepository.save(cart);

        return mapToResponse(
                savedOrder,
                request.getShippingAddressId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            UUID userId,
            UUID orderId
    ) {

        Order order =
                orderRepository.findByIdAndUserId(
                                orderId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        return mapToResponse(
                order,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(
            UUID userId
    ) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException(
                    "User not found"
            );
        }

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    @Auditable(
            action = "UPDATE_ORDER_STATUS",
            entity = "ORDER"
    )
    public OrderResponse updateOrderStatus(
            UUID orderId,
            OrderStatus status
    ) {

        if (status == null) {

            throw new BadRequestException(
                    "Order status is required"
            );
        }

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        validateStatusTransition(
                order.getStatus(),
                status
        );

        order.setStatus(status);

        Order savedOrder =
                orderRepository.save(order);

        return mapToResponse(
                savedOrder,
                null
        );
    }

    @Override
    @Auditable(
            action = "CANCEL_ORDER",
            entity = "ORDER"
    )
    public OrderResponse cancelOrder(
            UUID userId,
            UUID orderId
    ) {

        Order order =
                orderRepository.findByIdAndUserId(
                                orderId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (order.getStatus() !=
                OrderStatus.PENDING) {

            throw new BadRequestException(
                    "Only pending orders can be cancelled"
            );
        }

        /*
         * Restore stock because stock was deducted
         * when the order was placed.
         */
        for (OrderItem orderItem :
                order.getItems()) {

            inventoryService.increaseStock(
                    orderItem.getBook().getId(),
                    orderItem.getQuantity()
            );
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        Order savedOrder =
                orderRepository.save(order);

        return mapToResponse(
                savedOrder,
                null
        );
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus ==
                OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cancelled orders cannot change status"
            );
        }

        if (currentStatus ==
                OrderStatus.DELIVERED) {

            throw new BadRequestException(
                    "Delivered orders cannot change status"
            );
        }

        if (newStatus ==
                OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Use cancel order to cancel the order"
            );
        }

        if (newStatus ==
                OrderStatus.PENDING &&
                currentStatus !=
                        OrderStatus.PENDING) {

            throw new BadRequestException(
                    "Order cannot move back to PENDING"
            );
        }
    }

    private String generateUniqueOrderNumber() {

        String orderNumber;

        do {

            orderNumber =
                    "ORD-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                orderRepository.existsByOrderNumber(
                        orderNumber
                )
        );

        return orderNumber;
    }

    private OrderResponse mapToResponse(
            Order order,
            UUID shippingAddressId
    ) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(this::mapToItemResponse)
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(
                        order.getUser().getId()
                )
                .items(itemResponses)
                .shippingAddressId(
                        shippingAddressId
                )
                .totalAmount(
                        order.getTotalAmount()
                )
                .orderStatus(
                        order.getStatus().name()
                )
                .paymentStatus(
                        PaymentStatus.PENDING.name()
                )
                .createdAt(
                        order.getCreatedAt()
                )
                .updatedAt(
                        order.getUpdatedAt()
                )
                .build();
    }

    private OrderItemResponse mapToItemResponse(
            OrderItem orderItem
    ) {

        Book book =
                orderItem.getBook();

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .bookId(
                        book.getId()
                )
                .bookTitle(
                        book.getTitle()
                )
                .unitPrice(
                        orderItem.getUnitPrice()
                )
                .quantity(
                        orderItem.getQuantity()
                )
                .subtotal(
                        orderItem.getSubtotal()
                )
                .build();
    }

    private OrderSummaryResponse mapToSummaryResponse(
            Order order
    ) {

        int itemCount =
                order.getItems()
                        .stream()
                        .mapToInt(
                                OrderItem::getQuantity
                        )
                        .sum();

        return OrderSummaryResponse.builder()
                .id(order.getId())
                .totalAmount(
                        order.getTotalAmount()
                )
                .itemCount(itemCount)
                .orderStatus(
                        order.getStatus().name()
                )
                .paymentStatus(
                        PaymentStatus.PENDING.name()
                )
                .createdAt(
                        order.getCreatedAt()
                )
                .build();
    }
}