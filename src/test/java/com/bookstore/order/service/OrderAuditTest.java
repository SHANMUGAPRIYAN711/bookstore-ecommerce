package com.bookstore.order.service;

import com.bookstore.address.entity.Address;
import com.bookstore.address.repository.AddressRepository;
import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import com.bookstore.book.entity.Book;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.enums.AddressType;
import com.bookstore.common.enums.BookStatus;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.bookstore.notification.producer.NotificationProducer;

@ExtendWith(MockitoExtension.class)
class OrderAuditTest {

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

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private AuditService auditService;

    private OrderService orderService;

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

        OrderServiceImpl target =
                new OrderServiceImpl(
                        orderRepository,
                        userRepository,
                        addressRepository,
                        cartRepository,
                        inventoryService,
                        notificationProducer
                );

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        orderService =
                factory.getProxy();

        user =
                User.builder()
                        .firstName("Sauvik")
                        .lastName("Nandi")
                        .email("sauvik@bookstore.com")
                        .password("encoded-password")
                        .build();

        user.setId(userId);

        address =
                Address.builder()
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

        book =
                Book.builder()
                        .title("Clean Code")
                        .isbn("9780132350884")
                        .author("Robert Martin")
                        .description(
                                "Software craftsmanship"
                        )
                        .category("Programming")
                        .price(
                                new BigDecimal("500.00")
                        )
                        .stockQuantity(10)
                        .imageUrl(
                                "clean-code.jpg"
                        )
                        .status(BookStatus.ACTIVE)
                        .build();

        book.setId(bookId);

        cart =
                Cart.builder()
                        .user(user)
                        .items(
                                new ArrayList<>()
                        )
                        .build();

        cart.setId(cartId);

        CartItem cartItem =
                CartItem.builder()
                        .cart(cart)
                        .book(book)
                        .quantity(2)
                        .build();

        cartItem.setId(
                UUID.randomUUID()
        );

        cart.getItems().add(
                cartItem
        );

        order =
                Order.builder()
                        .user(user)
                        .orderNumber(
                                "ORD-12345678"
                        )
                        .status(
                                OrderStatus.PENDING
                        )
                        .subtotal(
                                new BigDecimal("1000.00")
                        )
                        .shippingFee(
                                BigDecimal.ZERO
                        )
                        .totalAmount(
                                new BigDecimal("1000.00")
                        )
                        .shippingAddressLine1(
                                "123 Main Street"
                        )
                        .shippingAddressLine2(
                                "Apartment 4B"
                        )
                        .shippingCity(
                                "Kolkata"
                        )
                        .shippingState(
                                "West Bengal"
                        )
                        .shippingPostalCode(
                                "700001"
                        )
                        .shippingCountry(
                                "India"
                        )
                        .items(
                                new ArrayList<>()
                        )
                        .build();

        order.setId(orderId);

        orderItem =
                OrderItem.builder()
                        .order(order)
                        .book(book)
                        .quantity(2)
                        .unitPrice(
                                new BigDecimal("500.00")
                        )
                        .subtotal(
                                new BigDecimal("1000.00")
                        )
                        .build();

        orderItem.setId(orderItemId);

        order.getItems().add(
                orderItem
        );

        configureSecurity();
    }

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();

        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // CREATE ORDER AUDIT
    // ============================================================

    @Test
    void placeOrder_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "POST",
                "/api/orders"
        );

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(user)
                );

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.of(address)
        );

        when(cartRepository.findByUserId(userId))
                .thenReturn(
                        Optional.of(cart)
                );

        when(orderRepository.existsByOrderNumber(
                anyString()
        )).thenReturn(false);

        when(inventoryService.hasSufficientStock(
                bookId,
                2
        )).thenReturn(true);

        when(orderRepository.save(
                any(Order.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        orderService.placeOrder(
                userId,
                CheckoutRequest.builder()
                        .shippingAddressId(
                                addressId
                        )
                        .build()
        );

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "CREATE_ORDER",
                auditLog.getAction()
        );

        assertEquals(
                "ORDER",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/orders",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    // ============================================================
    // UPDATE STATUS AUDIT
    // ============================================================

    @Test
    void updateOrderStatus_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "PATCH",
                "/api/orders/"
                        + orderId
                        + "/status"
        );

        when(orderRepository.findById(orderId))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderRepository.save(order))
                .thenReturn(order);

        orderService.updateOrderStatus(
                orderId,
                OrderStatus.CONFIRMED
        );

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "UPDATE_ORDER_STATUS",
                auditLog.getAction()
        );

        assertEquals(
                "ORDER",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "PATCH",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/orders/"
                        + orderId
                        + "/status",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    // ============================================================
    // CANCEL ORDER AUDIT
    // ============================================================

    @Test
    void cancelOrder_shouldCreateSuccessfulAuditLog() {

        configureRequest(
                "PATCH",
                "/api/orders/"
                        + orderId
                        + "/cancel"
        );

        when(orderRepository.findByIdAndUserId(
                orderId,
                userId
        )).thenReturn(
                Optional.of(order)
        );

        when(orderRepository.save(order))
                .thenReturn(order);

        orderService.cancelOrder(
                userId,
                orderId
        );

        AuditLog auditLog =
                captureAuditLog();

        assertEquals(
                "CANCEL_ORDER",
                auditLog.getAction()
        );

        assertEquals(
                "ORDER",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "PATCH",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/orders/"
                        + orderId
                        + "/cancel",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    private AuditLog captureAuditLog() {

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(
                        AuditLog.class
                );

        verify(auditService)
                .save(captor.capture());

        return captor.getValue();
    }

    private void configureSecurity() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "sauvik@bookstore.com",
                        null,
                        Collections.emptyList()
                );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                securityContext
        );
    }

    private void configureRequest(
            String method,
            String uri
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod(method);

        request.setRequestURI(uri);

        request.setRemoteAddr(
                "192.168.1.10"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(
                        request
                )
        );
    }
}