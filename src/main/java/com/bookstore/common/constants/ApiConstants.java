package com.bookstore.common.constants;

/**
 * Contains application-wide constants used for REST API
 * endpoint paths and API-related configuration.
 */
public final class ApiConstants {

    /**
     * Prevents instantiation of this utility class.
     */
    private ApiConstants() {
    }

    /**
     * Base path for all bookstore REST APIs.
     */
    public static final String API_BASE_PATH = "/api";

    /**
     * Base path for user-related APIs.
     */
    public static final String USERS_PATH = API_BASE_PATH + "/users";

    /**
     * Base path for address-related APIs.
     */
    public static final String ADDRESSES_PATH = API_BASE_PATH + "/addresses";

    /**
     * Base path for book-related APIs.
     */
    public static final String BOOKS_PATH = API_BASE_PATH + "/books";

    /**
     * Base path for cart-related APIs.
     */
    public static final String CART_PATH = API_BASE_PATH + "/cart";

    /**
     * Base path for wishlist-related APIs.
     */
    public static final String WISHLIST_PATH = API_BASE_PATH + "/wishlist";

    /**
     * Base path for order-related APIs.
     */
    public static final String ORDERS_PATH = API_BASE_PATH + "/orders";

    /**
     * Base path for review-related APIs.
     */
    public static final String REVIEWS_PATH = API_BASE_PATH + "/reviews";

    /**
     * Base path for inventory-related APIs.
     */
    public static final String INVENTORY_PATH = API_BASE_PATH + "/inventory";

    /**
     * Base path for authentication-related APIs.
     */
    public static final String AUTH_PATH = API_BASE_PATH + "/auth";
}