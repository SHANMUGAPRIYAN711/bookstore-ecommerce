package com.bookstore.user.entity;

import com.bookstore.common.entity.BaseEntity;
import com.bookstore.common.enums.AuthProvider;
import com.bookstore.common.enums.Role;
import com.bookstore.common.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bookstore.address.entity.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import jakarta.persistence.OneToMany;
import com.bookstore.cart.entity.Cart;
import jakarta.persistence.OneToOne;

import com.bookstore.order.entity.Order;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents an application user in the Bookstore system.
 *
 * <p>
 * A user may access the application as a customer, administrator,
 * or inventory helper depending on the assigned role.
 * </p>
 *
 * <p>
 * The entity also stores authentication-related information required
 * for both local username/password authentication and OAuth2/SSO
 * authentication.
 * </p>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    /**
     * User's first name.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * User's last name.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Unique email address used to identify the user's account.
     */
    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    /**
     * Password hash used for local authentication.
     *
     * <p>
     * This field must contain only a securely encoded password hash.
     * Plain-text passwords must never be stored in the database.
     * </p>
     *
     * <p>
     * The value may be null for users whose account is created
     * exclusively through an OAuth2/SSO provider.
     * </p>
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * User's phone number.
     */
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    /**
     * Application role assigned to the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    /**
     * Current status of the user account.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Authentication provider associated with the account.
     *
     * <p>
     * LOCAL represents traditional email/password authentication,
     * while values such as GOOGLE represent OAuth2/SSO authentication.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 30)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    /**
     * Unique identifier supplied by the external OAuth2 provider.
     *
     * <p>
     * This value is normally populated for SSO users and remains null
     * for traditional local accounts.
     * </p>
     */
    @Column(name = "provider_id", length = 255)
    private String providerId;

    /**
     * Addresses registered by this user.
     *
     * <p>
     * A user may have multiple delivery addresses. Address records are
     * owned by the user lifecycle and are removed when the corresponding
     * user is deleted.
     * </p>
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    /**
     * Shopping cart owned by this user.
     *
     * <p>
     * A user can have only one persistent shopping cart.
     * </p>
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Cart cart;

    /**
     * Orders placed by this user.
     *
     * <p>
     * A customer can place multiple orders over the lifetime of the
     * account. Orders remain persisted independently of the user's
     * current cart and address information.
     * </p>
     */
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}