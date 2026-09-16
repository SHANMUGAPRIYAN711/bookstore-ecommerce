package com.bookstore.book.service;

import com.bookstore.book.dto.BookCacheData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles Redis caching operations for books.
 *
 * <p>
 * This service provides a dedicated caching layer between the book service
 * and Redis. The database remains the source of truth, while Redis is used
 * to improve read performance for frequently requested book details.
 * </p>
 *
 * <p>
 * Redis failures are handled gracefully. If Redis is unavailable, the
 * application can continue using PostgreSQL as the primary data source.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookCacheService {

    /**
     * Prefix used for all book cache keys.
     *
     * <p>
     * Example:
     * {@code book:550e8400-e29b-41d4-a716-446655440000}
     * </p>
     */
    private static final String BOOK_CACHE_KEY_PREFIX = "book:";

    /**
     * Amount of time a book remains in Redis before automatic expiration.
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * Redis template used to read and write cached book data.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Retrieves a book from Redis using its ID.
     *
     * @param bookId unique identifier of the book
     * @return cached book data if present; otherwise an empty Optional
     */
    public Optional<BookCacheData> getBook(UUID bookId) {

        if (bookId == null) {
            return Optional.empty();
        }

        String cacheKey = buildKey(bookId);

        try {
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

            if (cachedValue instanceof BookCacheData bookCacheData) {

                log.debug("Book cache HIT for key: {}", cacheKey);

                return Optional.of(bookCacheData);
            }

            if (cachedValue != null) {

                log.warn(
                        "Unexpected value found in book cache for key: {}. Removing invalid entry.",
                        cacheKey
                );

                redisTemplate.delete(cacheKey);
            }

            log.debug("Book cache MISS for key: {}", cacheKey);

            return Optional.empty();

        } catch (DataAccessException exception) {

            log.warn(
                    "Redis unavailable while reading book cache for key: {}. Falling back to database.",
                    cacheKey,
                    exception
            );

            return Optional.empty();
        }
    }

    /**
     * Stores book data in Redis.
     *
     * <p>
     * The cached entry automatically expires after the configured TTL.
     * </p>
     *
     * @param book book data to cache
     */
    public void putBook(BookCacheData book) {

        if (book == null || book.getId() == null) {
            return;
        }

        String cacheKey = buildKey(book.getId());

        try {

            redisTemplate.opsForValue().set(
                    cacheKey,
                    book,
                    CACHE_TTL
            );

            log.debug(
                    "Book cached successfully. key={}, ttl={}",
                    cacheKey,
                    CACHE_TTL
            );

        } catch (DataAccessException exception) {

            log.warn(
                    "Redis unavailable while caching book with key: {}. Continuing without cache.",
                    cacheKey,
                    exception
            );
        }
    }

    /**
     * Removes a book from Redis.
     *
     * <p>
     * This method is called when a book is updated, deleted, or its
     * inventory quantity changes.
     * </p>
     *
     * @param bookId unique identifier of the book
     */
    public void evictBook(UUID bookId) {

        if (bookId == null) {
            return;
        }

        String cacheKey = buildKey(bookId);

        try {

            Boolean deleted = redisTemplate.delete(cacheKey);

            log.debug(
                    "Book cache eviction completed. key={}, deleted={}",
                    cacheKey,
                    deleted
            );

        } catch (DataAccessException exception) {

            log.warn(
                    "Redis unavailable while evicting book cache for key: {}.",
                    cacheKey,
                    exception
            );
        }
    }

    /**
     * Builds the Redis key for a book.
     *
     * @param bookId unique identifier of the book
     * @return Redis key in the format {@code book:{bookId}}
     */
    private String buildKey(UUID bookId) {
        return BOOK_CACHE_KEY_PREFIX + bookId;
    }
}