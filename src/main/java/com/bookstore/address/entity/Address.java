package com.bookstore.address.entity;

import com.bookstore.common.entity.BaseEntity;
import com.bookstore.common.enums.AddressType;
import com.bookstore.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical address belonging to a bookstore user.
 *
 * <p>
 * A user can maintain multiple addresses, such as a home address,
 * work address, or another delivery address. An address belongs to
 * exactly one user.
 * </p>
 *
 * <p>
 * Addresses are used primarily during checkout and order processing.
 * The address stored with an order will later be represented as an
 * order-specific snapshot so that historical orders are not affected
 * when a user's address is changed.
 * </p>
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    /**
     * User who owns this address.
     *
     * <p>
     * This is the owning side of the User-to-Address relationship.
     * The foreign key is stored in the {@code user_id} column.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Type of address, such as HOME, WORK, or OTHER.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "address_type",
            nullable = false,
            length = 30
    )
    private AddressType addressType;

    /**
     * First line of the postal address.
     */
    @Column(
            name = "address_line1",
            nullable = false,
            length = 255
    )
    private String addressLine1;

    /**
     * Optional second line of the postal address.
     */
    @Column(
            name = "address_line2",
            length = 255
    )
    private String addressLine2;

    /**
     * City in which the address is located.
     */
    @Column(
            name = "city",
            nullable = false,
            length = 100
    )
    private String city;

    /**
     * State or province in which the address is located.
     */
    @Column(
            name = "state",
            nullable = false,
            length = 100
    )
    private String state;

    /**
     * Postal or ZIP code of the address.
     */
    @Column(
            name = "postal_code",
            nullable = false,
            length = 20
    )
    private String postalCode;

    /**
     * Country in which the address is located.
     */
    @Column(
            name = "country",
            nullable = false,
            length = 100
    )
    private String country;

    /**
     * Indicates whether this is the user's default address.
     *
     * <p>
     * A user may have multiple addresses but the service layer will
     * enforce the business rule that only one address can be the
     * default address at a time.
     * </p>
     */
    @Column(
            name = "is_default",
            nullable = false
    )
    @Builder.Default
    private boolean defaultAddress = false;
}