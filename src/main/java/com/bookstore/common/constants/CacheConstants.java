package com.bookstore.common.constants;

/**
 * Contains constants used for Redis cache names and cache key prefixes.
 */
public final class CacheConstants {

    /**
     * Prevents instantiation of this utility class.
     */
    private CacheConstants() {
    }

    /**
     * Cache containing frequently accessed book information.
     */
    public static final String BOOK_CACHE = "books";

    /**
     * Cache containing book catalog information.
     */
    public static final String BOOK_CATALOG_CACHE = "bookCatalog";

    /**
     * Cache containing user information.
     */
    public static final String USER_CACHE = "users";

    /**
     * Cache containing shopping cart information.
     */
    public static final String CART_CACHE = "carts";

    /**
     * Cache containing wishlist information.
     */
    public static final String WISHLIST_CACHE = "wishlists";

    /**
     * Prefix used when constructing cart-related Redis keys.
     */
    public static final String CART_KEY_PREFIX = "cart:";

    /**
     * Prefix used when constructing wishlist-related Redis keys.
     */
    public static final String WISHLIST_KEY_PREFIX = "wishlist:";

    /**
     * Prefix used when constructing user-related Redis keys.
     */
    public static final String USER_KEY_PREFIX = "user:";
}