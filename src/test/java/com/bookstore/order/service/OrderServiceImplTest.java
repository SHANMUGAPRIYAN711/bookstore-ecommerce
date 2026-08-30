package com.bookstore.order.service;

import com.bookstore.address.entity.Address;
import com.bookstore.address.repository.AddressRepository;
import com.bookstore.book.entity.Book;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.enums.AddressType;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummaryResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID addressId;
    private UUID bookId;
    private UUID cartId;
    private UUID orderId;
    private UUID orderItemId;

    private User user;
    private Address address;
    private Book book;
    private Cart cart;
    private CartItem cartItem;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        orderItemId = UUID.randomUUID();

        user = User.builder()
                .firstName("Sauvik")
                .lastName("Nandi")
                .email("sauvik@bookstore.com")
                .password("encoded-password")
                .build();

        user.setId(userId);

        address = Address.builder()
                .user(user)
                .addressType(AddressType.HOME)
                .addressLine1("123 Main Street")
                .addressLine2("Apartment 4B")
                .city("Kolkata")
                .state("West Bengal")
                .postalCode("700001")
                .country("India")
                .defaultAddress(true)
                .build();

        address.setId(addressId);

        book = Book.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("Software craftsmanship")
                .category("Programming")
                .price(new BigDecimal("500.00"))
                .stockQuantity(10)
                .imageUrl("https://example.com/clean-code.jpg")
                .status(BookStatus.ACTIVE)
                .build();

        book.setId(bookId);

        cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        cart.setId(cartId);

        cartItem = CartItem.builder()
                .cart(cart)
                .book(book)
                .quantity(2)
                .build();

        cartItem.setId(UUID.randomUUID());

        cart.getItems().add(cartItem);

        order = Order.builder()
                .user(user)
                .orderNumber("ORD-12345678")
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("1000.00"))
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1000.00"))
                .shippingAddressLine1("123 Main Street")
                .shippingAddressLine2("Apartment 4B")
                .shippingCity("Kolkata")
                .shippingState("West Bengal")
                .shippingPostalCode("700001")
                .shippingCountry("India")
                .items(new ArrayList<>())
                .build();

        order.setId(orderId);

        orderItem = OrderItem.builder()
                .order(order)
                .book(book)
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();

        orderItem.setId(orderItemId);

        order.getItems().add(orderItem);
    }

    // ============================================================
    // PLACE ORDER
    // ============================================================

    @Test
    void placeOrder_shouldCreateOrderSuccessfully() {

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.existsByOrderNumber(anyString()))
                .thenReturn(false);

        when(inventoryService.hasSufficientStock(
                bookId,
                2
        )).thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.placeOrder(
                        userId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                userId,
                response.getUserId()
        );

        assertEquals(
                addressId,
                response.getShippingAddressId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getTotalAmount()
        );

        assertEquals(
                "PENDING",
                response.getOrderStatus()
        );

        assertEquals(
                "PENDING",
                response.getPaymentStatus()
        );

        assertEquals(
                1,
                response.getItems().size()
        );

        verify(userRepository)
                .findById(userId);

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verify(cartRepository)
                .findByUserId(userId);

        verify(inventoryService)
                .hasSufficientStock(
                        bookId,
                        2
                );

        verify(inventoryService)
                .decreaseStock(
                        bookId,
                        2
                );

        verify(orderRepository)
                .save(any(Order.class));

        verify(cartRepository)
                .save(cart);

        assertTrue(
                cart.getItems().isEmpty()
        );
    }

    @Test
    void placeOrder_shouldCalculateTotalForMultipleItems() {

        Book secondBook =
                Book.builder()
                        .title("Effective Java")
                        .isbn("9780134685991")
                        .author("Joshua Bloch")
                        .description("Java best practices")
                        .category("Programming")
                        .price(new BigDecimal("300.00"))
                        .stockQuantity(10)
                        .imageUrl("java.jpg")
                        .status(BookStatus.ACTIVE)
                        .build();

        UUID secondBookId = UUID.randomUUID();

        secondBook.setId(secondBookId);

        CartItem secondCartItem =
                CartItem.builder()
                        .cart(cart)
                        .book(secondBook)
                        .quantity(3)
                        .build();

        cart.getItems().add(secondCartItem);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.existsByOrderNumber(anyString()))
                .thenReturn(false);

        when(inventoryService.hasSufficientStock(
                any(UUID.class),
                anyInt()
        )).thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.placeOrder(
                        userId,
                        CheckoutRequest.builder()
                                .shippingAddressId(addressId)
                                .build()
                );

        /*
         * First item:
         * 2 × 500 = 1000
         *
         * Second item:
         * 3 × 300 = 900
         *
         * Total:
         * 1900
         */
        assertEquals(
                new BigDecimal("1900.00"),
                response.getTotalAmount()
        );

        assertEquals(
                2,
                response.getItems().size()
        );
    }

    @Test
    void placeOrder_shouldRejectNullRequest() {

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(
                        userId,
                        null
                )
        );

        verifyNoInteractions(
                userRepository,
                addressRepository,
                cartRepository,
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldRejectMissingShippingAddress() {

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verifyNoInteractions(
                userRepository,
                addressRepository,
                cartRepository,
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldThrowWhenUserDoesNotExist() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(
                addressRepository,
                cartRepository,
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldThrowWhenAddressDoesNotBelongToUser() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.empty());

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verifyNoInteractions(
                cartRepository,
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldThrowWhenCartDoesNotExist() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verify(cartRepository)
                .findByUserId(userId);

        verifyNoInteractions(
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldRejectEmptyCart() {

        cart.getItems().clear();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verifyNoInteractions(
                inventoryService,
                orderRepository
        );
    }

    @Test
    void placeOrder_shouldRejectInvalidCartItemQuantity() {

        cartItem.setQuantity(0);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verify(inventoryService, never())
                .hasSufficientStock(
                        any(UUID.class),
                        anyInt()
                );
    }

    @Test
    void placeOrder_shouldRejectInsufficientStock() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(inventoryService.hasSufficientStock(
                bookId,
                2
        )).thenReturn(false);

        CheckoutRequest request =
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(
                        userId,
                        request
                )
        );

        verify(inventoryService)
                .hasSufficientStock(
                        bookId,
                        2
                );

        verify(inventoryService, never())
                .decreaseStock(
                        any(UUID.class),
                        anyInt()
                );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void placeOrder_shouldDecreaseInventory() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.existsByOrderNumber(anyString()))
                .thenReturn(false);

        when(inventoryService.hasSufficientStock(
                bookId,
                2
        )).thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        orderService.placeOrder(
                userId,
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build()
        );

        verify(inventoryService)
                .decreaseStock(
                        bookId,
                        2
                );
    }

    @Test
    void placeOrder_shouldClearCartAfterSuccessfulOrder() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.existsByOrderNumber(anyString()))
                .thenReturn(false);

        when(inventoryService.hasSufficientStock(
                bookId,
                2
        )).thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        orderService.placeOrder(
                userId,
                CheckoutRequest.builder()
                        .shippingAddressId(addressId)
                        .build()
        );

        assertTrue(
                cart.getItems().isEmpty()
        );

        verify(cartRepository)
                .save(cart);
    }

    // ============================================================
    // GET ORDER
    // ============================================================

    @Test
    void getOrder_shouldReturnOrder() {

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getOrder(
                        userId,
                        orderId
                );

        assertNotNull(response);

        assertEquals(
                orderId,
                response.getId()
        );

        assertEquals(
                userId,
                response.getUserId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getTotalAmount()
        );

        assertEquals(
                "PENDING",
                response.getOrderStatus()
        );

        assertEquals(
                "PENDING",
                response.getPaymentStatus()
        );

        assertEquals(
                1,
                response.getItems().size()
        );
    }

    @Test
    void getOrder_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrder(
                        userId,
                        orderId
                )
        );

        verify(orderRepository)
                .findByIdAndUserId(
                        orderId,
                        userId
                );
    }

    // ============================================================
    // GET USER ORDERS
    // ============================================================

    @Test
    void getOrders_shouldReturnOrderHistory() {

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(order));

        List<OrderSummaryResponse> response =
                orderService.getOrders(userId);

        assertNotNull(response);

        assertEquals(
                1,
                response.size()
        );

        OrderSummaryResponse summary =
                response.get(0);

        assertEquals(
                orderId,
                summary.getId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                summary.getTotalAmount()
        );

        assertEquals(
                2,
                summary.getItemCount()
        );

        assertEquals(
                "PENDING",
                summary.getOrderStatus()
        );

        assertEquals(
                "PENDING",
                summary.getPaymentStatus()
        );
    }

    @Test
    void getOrders_shouldReturnEmptyList() {

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<OrderSummaryResponse> response =
                orderService.getOrders(userId);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getOrders_shouldThrowWhenUserDoesNotExist() {

        when(userRepository.existsById(userId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrders(userId)
        );

        verify(orderRepository, never())
                .findByUserIdOrderByCreatedAtDesc(
                        any(UUID.class)
                );
    }

    // ============================================================
    // UPDATE ORDER STATUS
    // ============================================================

    @Test
    void updateOrderStatus_shouldUpdateSuccessfully() {

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                );

        assertEquals(
                OrderStatus.CONFIRMED,
                order.getStatus()
        );

        assertEquals(
                "CONFIRMED",
                response.getOrderStatus()
        );

        verify(orderRepository)
                .findById(orderId);

        verify(orderRepository)
                .save(order);
    }

    @Test
    void updateOrderStatus_shouldRejectNullStatus() {

        assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        null
                )
        );

        verifyNoInteractions(
                orderRepository
        );
    }

    @Test
    void updateOrderStatus_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                )
        );
    }

    @Test
    void updateOrderStatus_shouldRejectCancelledOrder() {

        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldRejectDeliveredOrder() {

        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldRejectDirectCancellation() {

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CANCELLED
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    // ============================================================
    // CANCEL ORDER
    // ============================================================

    @Test
    void cancelOrder_shouldCancelPendingOrder() {

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.cancelOrder(
                        userId,
                        orderId
                );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );

        assertEquals(
                "CANCELLED",
                response.getOrderStatus()
        );

        verify(inventoryService)
                .increaseStock(
                        bookId,
                        2
                );

        verify(orderRepository)
                .save(order);
    }

    @Test
    void cancelOrder_shouldRejectNonPendingOrder() {

        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.cancelOrder(
                        userId,
                        orderId
                )
        );

        verify(inventoryService, never())
                .increaseStock(
                        any(UUID.class),
                        anyInt()
                );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancelOrder(
                        userId,
                        orderId
                )
        );

        verifyNoInteractions(
                inventoryService
        );
    }
}