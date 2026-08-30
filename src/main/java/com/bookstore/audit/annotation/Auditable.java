package com.bookstore.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an auditable business operation.
 *
 * <p>
 * Methods annotated with {@code @Auditable} can be intercepted
 * by {@code AuditAspect} so that important business activities
 * can be recorded.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {@code
 * @Auditable(
 *      action = "CREATE_BOOK",
 *      entity = "BOOK"
 * )
 * public BookResponse createBook(...) {
 *     ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Describes the business operation being performed.
     *
     * <p>
     * Examples:
     * CREATE_BOOK,
     * UPDATE_BOOK,
     * DELETE_REVIEW,
     * UPDATE_STOCK.
     * </p>
     *
     * @return audit action name
     */
    String action();

    /**
     * Identifies the business entity affected by the operation.
     *
     * <p>
     * Examples:
     * BOOK,
     * INVENTORY,
     * REVIEW,
     * USER.
     * </p>
     *
     * @return entity name
     */
    String entity() default "";
}