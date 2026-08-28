package com.bookstore.order.entity;

import com.bookstore.common.entity.BaseEntity;
import com.bookstore.common.enums.OrderStatus;
import com.bookstore.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order placed through the Bookstore application.
 *
 * <p>
 * An order records the products purchased by a customer, the prices
 * applicable at the time of purchase, the shipping address used for
 * delivery, and the current lifecycle status of the order.
 * </p>
 *
 * <p>
 * The shipping address is stored as a snapshot rather than as a
 * reference to the user's current Address entity. This ensures that
 * historical order information remains unchanged if the customer
 * later modifies or deletes their saved address.
 * </p>
 */
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_order_number",
                        columnNames = "order_number"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    /**
     * Customer who placed the order.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Human-readable unique identifier for the order.
     *
     * <p>
     * This identifier is intended for customer-facing order references
     * and should not be used as the database primary key.
     * </p>
     */
    @Column(
            name = "order_number",
            nullable = false,
            length = 50
    )
    private String orderNumber;

    /**
     * Current lifecycle status of the order.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Total value of all order items before shipping charges.
     */
    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    /**
     * Shipping charge applied to the order.
     */
    @Column(
            name = "shipping_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /**
     * Final amount payable by the customer.
     *
     * <p>
     * This value represents the subtotal plus applicable shipping
     * charges and any other amounts included by the checkout process.
     * </p>
     */
    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    /**
     * First line of the shipping address captured at checkout.
     */
    @Column(
            name = "shipping_address_line1",
            nullable = false,
            length = 255
    )
    private String shippingAddressLine1;

    /**
     * Optional second line of the shipping address.
     */
    @Column(
            name = "shipping_address_line2",
            length = 255
    )
    private String shippingAddressLine2;

    /**
     * City of the shipping address.
     */
    @Column(
            name = "shipping_city",
            nullable = false,
            length = 100
    )
    private String shippingCity;

    /**
     * State of the shipping address.
     */
    @Column(
            name = "shipping_state",
            nullable = false,
            length = 100
    )
    private String shippingState;

    /**
     * Postal code of the shipping address.
     */
    @Column(
            name = "shipping_postal_code",
            nullable = false,
            length = 20
    )
    private String shippingPostalCode;

    /**
     * Country of the shipping address.
     */
    @Column(
            name = "shipping_country",
            nullable = false,
            length = 100
    )
    private String shippingCountry;

    /**
     * Items purchased as part of this order.
     *
     * <p>
     * Order items are lifecycle-dependent on their parent order.
     * </p>
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}