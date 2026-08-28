package com.bookstore.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Provides reusable date and time operations used throughout
 * the Bookstore application.
 */
public final class DateTimeUtil {

    /**
     * Prevents instantiation of this utility class.
     */
    private DateTimeUtil() {
    }

    /**
     * Returns the current timestamp in UTC.
     *
     * @return current UTC timestamp as an Instant
     */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /**
     * Converts a LocalDateTime value to an UTC Instant.
     *
     * @param dateTime local date and time to convert
     * @return corresponding UTC Instant
     */
    public static Instant toUtc(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.toInstant(ZoneOffset.UTC);
    }

    /**
     * Converts an Instant to a LocalDateTime using UTC.
     *
     * @param instant timestamp to convert
     * @return corresponding UTC LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}